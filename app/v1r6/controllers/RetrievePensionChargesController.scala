/*
 * Copyright 2022 HM Revenue & Customs
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

package v1r6.controllers

import cats.data.EitherT
import javax.inject._
import play.api.http.MimeTypes
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import utils.IdGenerator
import v1r6.controllers.requestParsers.RetrievePensionChargesParser
import v1r6.hateoas.HateoasFactory
import v1r6.models.errors._
import v1r6.models.request.RetrievePensionCharges.RetrievePensionChargesRawData
import v1r6.models.response.retrieve.RetrievePensionChargesHateoasData
import v1r6.services._

import scala.concurrent.{ExecutionContext, Future}

class RetrievePensionChargesController @Inject()(val authService: EnrolmentsAuthService,
                                                 val lookupService: MtdIdLookupService,
                                                 service: RetrievePensionChargesService,
                                                 requestParser: RetrievePensionChargesParser,
                                                 hateoasFactory: HateoasFactory,
                                                 cc: ControllerComponents, val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "RetrievePensionChargesController",
      endpointName = "Retrieve a Pensions Charge")

  def retrieve(nino: String, taxYear: String): Action[AnyContent] = {
    authorisedAction(nino).async { implicit request =>

      implicit val correlationId: String = idGenerator.generateCorrelationId
      logger.info(
        s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
          s"with CorrelationId: $correlationId")

      val rawData = RetrievePensionChargesRawData(nino, taxYear)

      val result = for {
        parsedRequest <- EitherT.fromEither[Future](requestParser.parseRequest(rawData))
        serviceResponse <- EitherT(service.retrievePensions(parsedRequest))
      } yield {
        logger.info(s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
          s"Success response received with CorrelationId: ${serviceResponse.correlationId}")

        val hateoasResponse = hateoasFactory.wrap(serviceResponse.responseData, RetrievePensionChargesHateoasData(nino,taxYear))

        Ok(Json.toJson(hateoasResponse)).withApiHeaders(serviceResponse.correlationId)
          .as(MimeTypes.JSON)
      }

      result.leftMap { errorWrapper =>
        val resCorrelationId = errorWrapper.correlationId
        val result = errorResult(errorWrapper).withApiHeaders(resCorrelationId)
        logger.warn(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Error response received with CorrelationId: $resCorrelationId")

        result
      }.merge
    }
  }

  private def errorResult(errorWrapper: ErrorWrapper): Result = {
    (errorWrapper.error: @unchecked) match {
      case BadRequestError | NinoFormatError |
           TaxYearFormatError | RuleTaxYearRangeInvalid |
           RuleTaxYearNotSupportedError
      => BadRequest(Json.toJson(errorWrapper))
      case NotFoundError => NotFound(Json.toJson(errorWrapper))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
    }
  }

}