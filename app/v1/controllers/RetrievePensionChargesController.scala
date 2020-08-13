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

  def retrieve(nino: String, taxYear: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      val rawData = RetrievePensionChargesRawData(nino, taxYear)
      val parsedRequest = EitherT.fromEither[Future](retrievePensionsChargesParser.parseRequest(rawData))
      val serviceResponse : Future[RetrievePensionsOutcome] = parsedRequest match {
        case Right(data) => service.deletePensionCharges(data)
        case Left(errorWrapper) => Future.successful(Left(errorWrapper))
      }
    }




  private def errorResult(errorWrapper: ErrorWrapper) = {
    (errorWrapper.errors.head: @unchecked) match {
      case BadRequestError | NinoFormatError | LossIdFormatError => BadRequest(Json.toJson(errorWrapper))
      case NotFoundError                                         => NotFound(Json.toJson(errorWrapper))
      case DownstreamError                                       => InternalServerError(Json.toJson(errorWrapper))
    }
  }



}
