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

import javax.inject._
import play.api.http.MimeTypes
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import v1.controllers.requestParsers.RetrievePensionChargesParser
import v1.hateoas.HateoasFactory
import v1.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import v1.models.auth.UserDetails
import v1.models.errors._
import v1.models.requestData.{RetrievePensionChargesRawData, RetrievePensionChargesRequest}
import v1.services._

import scala.concurrent.{ExecutionContext, Future}

class RetrievePensionChargesController @Inject()(val authService: EnrolmentsAuthService,
                                                val lookupService: MtdIdLookupService,
                                                service: RetrievePensionChargesService,
                                                requestParser: RetrievePensionChargesParser,
                                                hateoasFactory: HateoasFactory,
                                                auditService: AuditService,
                                                cc: ControllerComponents)(implicit ec: ExecutionContext) extends AuthorisedController(cc) with BaseController {
  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "RetrievePensionChargesController",
      endpointName = "Retrieve a Pensions Charge")

  //test
  def retrieve(nino: String, taxYear: String): Action[AnyContent] = {
    authorisedAction(nino).async { implicit request =>

      val rawData = RetrievePensionChargesRawData(nino, taxYear)
      val parseRequest: Either[ErrorWrapper, RetrievePensionChargesRequest] = requestParser.parseRequest(rawData)

      val serviceResponse: Future[RetrievePensionChargesOutcome] = parseRequest match {
        case Right(data) => service.retrievePensions(data)
        case Left(errorWrapper) => Future.successful(Left(errorWrapper))
      }

      serviceResponse.map {
        case Right(responseWrapper) =>

          logger.info(s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Success response received with CorrelationId: ${responseWrapper.correlationId}")

          auditSubmission(
            createAuditDetails(rawData, OK, responseWrapper.correlationId, request.userDetails, None, Some(Json.toJson(responseWrapper.correlationId)))
          )

          Ok(Json.toJson(responseWrapper.responseData)).withApiHeaders(responseWrapper.correlationId)
            .as(MimeTypes.JSON)

        case Left(errorWrapper) =>
          val correlationId = getCorrelationId(errorWrapper)
          val result = errorResult(errorWrapper).withApiHeaders(correlationId)
          auditSubmission(createAuditDetails(rawData, result.header.status, correlationId, request.userDetails, Some(errorWrapper)))
          result
      }
    }
  }

  private def errorResult(errorWrapper: ErrorWrapper): Result = {

    (errorWrapper.errors.head: @unchecked) match {
      case BadRequestError | NinoFormatError | TaxYearFormatError |
           RuleTaxYearRangeInvalid | RuleTaxYearNotSupportedError => BadRequest(Json.toJson(errorWrapper))
      case NotFoundError => NotFound(Json.toJson(errorWrapper))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
    }
  }

  private def createAuditDetails(rawData: RetrievePensionChargesRawData,
                                 statusCode: Int,
                                 correlationId: String,
                                 userDetails: UserDetails,
                                 errorWrapper: Option[ErrorWrapper] = None,
                                 responseBody: Option[JsValue] = None): GenericAuditDetail = {

    val response = errorWrapper.map( wrapper => AuditResponse(statusCode, Some(wrapper.auditErrors), None)).getOrElse(AuditResponse(statusCode, None, None))
    GenericAuditDetail(userDetails.userType, userDetails.agentReferenceNumber, rawData.nino, correlationId, response)
  }

  private def auditSubmission(details: GenericAuditDetail)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] = {
    val event = AuditEvent("deletePensionChargesAuditType", "delete-pension-charges-transaction-type", details)
    auditService.auditEvent(event)
  }






}
