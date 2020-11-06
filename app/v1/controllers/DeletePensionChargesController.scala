/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package v1.controllers

import javax.inject.Inject
import play.api.http.MimeTypes
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import utils.IdGenerator
import v1.controllers.requestParsers.DeletePensionChargesParser
import v1.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import v1.models.auth.UserDetails
import v1.models.errors._
import v1.models.requestData.{DeletePensionChargesRawData, DeletePensionChargesRequest}
import v1.services._

import scala.concurrent.{ExecutionContext, Future}

class DeletePensionChargesController @Inject()(val authService: EnrolmentsAuthService,
                                               val lookupService: MtdIdLookupService,
                                               requestParser: DeletePensionChargesParser,
                                               service: DeletePensionChargesService,
                                               auditService: AuditService,
                                               cc: ControllerComponents,
                                               val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController {

  implicit val endpointLogContext: EndpointLogContext = EndpointLogContext(
    controllerName = "DeletePensionChargesController",
    endpointName = "Delete Pension Charges"
  )

  def delete(nino: String, taxYear: String): Action[AnyContent] = {
    authorisedAction(nino).async { implicit request =>

      implicit val correlationId: String = idGenerator.generateCorrelationId
      logger.info(
        s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
          s"with CorrelationId: $correlationId")

      val rawData = DeletePensionChargesRawData(nino, taxYear)
      val parseRequest: Either[ErrorWrapper, DeletePensionChargesRequest] = requestParser.parseRequest(rawData)

      val serviceResponse: Future[DeletePensionChargesOutcome] = parseRequest match {
        case Right(data) => service.deletePensionCharges(data)
        case Left(errorWrapper) => Future.successful(Left(errorWrapper))
      }

      serviceResponse.map {
        case Right(responseWrapper) =>

          logger.info(s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with CorrelationId: ${responseWrapper.correlationId}")

          auditSubmission(createAuditDetails(
            rawData,
            NO_CONTENT,
            responseWrapper.correlationId,
            request.userDetails,
            None,
            None
          ))

          NoContent.withApiHeaders(responseWrapper.correlationId).as(MimeTypes.JSON)

        case Left(errorWrapper) =>

          val resCorrelationId = errorWrapper.correlationId
          val result = errorResult(errorWrapper).withApiHeaders(resCorrelationId)
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Error response received with CorrelationId: $resCorrelationId")

          auditSubmission(createAuditDetails(
            rawData,
            result.header.status,
            correlationId,
            request.userDetails,
            Some(errorWrapper),
            None
          ))

          result
      }
    }
  }

  private def errorResult(errorWrapper: ErrorWrapper): Result = {
    (errorWrapper.errors.head: @unchecked) match {
      case BadRequestError | NinoFormatError |
           TaxYearFormatError | RuleTaxYearRangeInvalid |
           RuleTaxYearNotSupportedError
      => BadRequest(Json.toJson(errorWrapper))
      case NotFoundError => NotFound(Json.toJson(errorWrapper))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
    }
  }

  private def createAuditDetails(rawData: DeletePensionChargesRawData,
                                 statusCode: Int,
                                 correlationId: String,
                                 userDetails: UserDetails,
                                 errorWrapper: Option[ErrorWrapper],
                                 responseBody: Option[JsValue]): GenericAuditDetail = {

    val response = errorWrapper.map(wrapper => AuditResponse(statusCode, Some(wrapper.auditErrors), None))
      .getOrElse(AuditResponse(statusCode, None, responseBody))

    GenericAuditDetail(
      userDetails.userType,
      userDetails.agentReferenceNumber,
      rawData.nino,
      rawData.taxYear,
      None,
      response,
      correlationId
    )
  }

  private def auditSubmission(details: GenericAuditDetail)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] = {
    val event = AuditEvent("DeletePensionsCharges", "delete-pensions-charges", details)
    auditService.auditEvent(event)
  }
}
