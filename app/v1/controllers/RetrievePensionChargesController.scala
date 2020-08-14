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

import cats.data.EitherT
import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import v1.hateoas.HateoasFactory
import v1.models.audit.{AuditResponse, GenericAuditDetail}
import v1.models.errors.ErrorWrapper
import v1.models.requestData.{RetrievePensionChargesRawData, RetrievePensionChargesRequest}
import v1.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService, RetrievePensionChargesService}

import scala.concurrent.{ExecutionContext, Future}

class RetrievePensionChargesController @Inject()(val authService: EnrolmentsAuthService,
                                                val lookupService: MtdIdLookupService,
                                                retrievePensionChargesService: RetrievePensionChargesService,
                                                retrievePensionsParser: ???,
                                                hateoasFactory: HateoasFactory,
                                                auditService: AuditService,
                                                cc: ControllerComponents)(implicit ec: ExecutionContext) extends AuthorisedController(cc) with BaseController {
  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "RetrievePensionChargesController", endpointName = "Retrieve a Pensions Charge")

  def retrieve(nino: String, taxYear: String): Action[AnyContent] = {
    authorisedAction(nino).async { implicit request =>
      val rawData = RetrievePensionChargesRawData(nino, taxYear)
      val parsedRequest = EitherT.fromEither[Future](retrievePensionsChargesParser.parseRequest(rawData))

      val serviceResponse : Future[RetrievePensionsOutcome] = parsedRequest match {
        case Right(data) => service.deletePensionCharges(data)
        case Left(errorWrapper) => Future.successful(Left(errorWrapper))
      }

      serviceResponse.map {
        case Right(responseWrapper)=>

          logger.info(s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Success response received with CorrelationId: ${responseWrapper.correlationId}")

          auditSubmission(
            createAuditDetails(rawData, OK, responseWrapper.correlationId, request.userDetails, None, Some(Json.toJson(responseWrapper.correlationId)))
          )

          Ok.withApiHeaders(responseWrapper.correlationId).as(MimeTypes.JSON)

        case Left(errorWrapper) =>
          val correlationId = getCorrelationId(errorWrapper)
          val result = errorResult(errorWrapper).withApiHeaders(correlationId)
          auditSubmission(createAuditDetails(rawData, result.header.status, correlationId, request.userDetails, Some(errorWrapper)))
          result

      }
    }
  }

  private def createAuditDetails(rawData: CreateRawData,
                                 statusCode: Int,
                                 correlationId: String,
                                 userDetails: UserDetails,
                                 errorWrapper: Option[ErrorWrapper] = None,
                                 responseBody: Option[JsValue] = None): GenericAuditDetail = {
    val response = errorWrapper
      .map { wrapper =>
        AuditResponse(statusCode, Some(wrapper.auditErrors), None)
      }
      .getOrElse(AuditResponse(statusCode, None, responseBody ))

    GenericAuditDetail(userDetails.userType, userDetails.agentReferenceNumber, rawData.nino, correlationId, response)
  }

  private def auditSubmission(details: GenericAuditDetail)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] = {
    val event = AuditEvent("createCisDeductionsAuditType", "create-cis-deductions-transaction-type", details)
    auditService.auditEvent(event)
  }
}
