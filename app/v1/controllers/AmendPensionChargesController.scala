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
import config.AppConfig
import javax.inject._
import play.api.http.MimeTypes
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContentAsJson, ControllerComponents, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import utils.IdGenerator
import v1.controllers.requestParsers.AmendPensionChargesParser
import v1.hateoas.AmendHateoasBody
import v1.models.audit._
import v1.models.auth.UserDetails
import v1.models.errors._
import v1.models.requestData.AmendPensionChargesRawData
import v1.services._

import scala.concurrent.{ExecutionContext, Future}

class AmendPensionChargesController @Inject()(val authService: EnrolmentsAuthService,
                                              val lookupService: MtdIdLookupService,
                                              service: AmendPensionChargesService,
                                              requestParser: AmendPensionChargesParser,
                                              auditService: AuditService,
                                              appConfig: AppConfig,
                                              cc: ControllerComponents,
                                              val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc) with AmendHateoasBody with BaseController {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "AmendPensionChargesController",
      endpointName = "Amend a Pensions Charge")

  def amend(nino: String, taxYear: String): Action[JsValue] = {
    authorisedAction(nino).async(parse.json) { implicit request =>

      implicit val correlationId: String = idGenerator.generateCorrelationId
      logger.info(
        s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
          s"with CorrelationId: $correlationId")

      val rawData = AmendPensionChargesRawData(nino, taxYear, AnyContentAsJson(request.body))

      val result = for {
        parsedRequest <- EitherT.fromEither[Future](requestParser.parseRequest(rawData))
        serviceResponse <- EitherT(service.amendPensions(parsedRequest))
      } yield {
        logger.info(s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
          s"Success response received with CorrelationId: ${serviceResponse.correlationId}")

        val responseWrapperWithHateoas = amendPensionsHateoasBody(appConfig, nino, taxYear)

        auditSubmission(createAuditDetails(
          rawData,
          OK,
          serviceResponse.correlationId,
          request.userDetails,
          None,
          Some(responseWrapperWithHateoas)
        ))

        Ok(responseWrapperWithHateoas).withApiHeaders(serviceResponse.correlationId).as(MimeTypes.JSON)
      }

      result.leftMap { errorWrapper =>
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
          Some(amendPensionsHateoasBody(appConfig, nino, taxYear))
        ))

        result
      }.merge
    }
  }

  private def errorResult(errorWrapper: ErrorWrapper): Result = {
    (errorWrapper.error: @unchecked) match {
      case BadRequestError | NinoFormatError |
           TaxYearFormatError | RuleTaxYearRangeInvalid |
           RuleTaxYearNotSupportedError | RuleIncorrectOrEmptyBodyError |
           MtdErrorWithCustomMessage(ValueFormatError.code) | MtdErrorWithCustomMessage(RuleCountryCodeError.code) |
           MtdErrorWithCustomMessage(CountryCodeFormatError.code) | MtdErrorWithCustomMessage(QOPSRefFormatError.code) |
           MtdErrorWithCustomMessage(PensionSchemeTaxRefFormatError.code) | MtdErrorWithCustomMessage(ProviderNameFormatError.code) |
           MtdErrorWithCustomMessage(ProviderAddressFormatError.code) | RuleIsAnnualAllowanceReducedError |
           RuleBenefitExcessesError | RulePensionReferenceError
      => BadRequest(Json.toJson(errorWrapper))
      case NotFoundError => NotFound(Json.toJson(errorWrapper))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
    }
  }

  private def createAuditDetails(rawData: AmendPensionChargesRawData,
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
      Some(rawData.body.json),
      response,
      correlationId
    )
  }

  private def auditSubmission(details: GenericAuditDetail)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] = {
    val event = AuditEvent("CreateAmendPensionsCharges", "create-amend-pensions-charges", details)
    auditService.auditEvent(event)
  }
}
