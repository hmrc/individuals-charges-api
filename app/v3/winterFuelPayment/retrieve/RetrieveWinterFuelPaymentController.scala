/*
 * Copyright 2026 HM Revenue & Customs
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

package v3.winterFuelPayment.retrieve

import api.config.AppConfig
import api.controllers.*
import api.services.*
import api.utils.IdGenerator
import play.api.mvc.*

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class RetrieveWinterFuelPaymentController @Inject() (val authService: EnrolmentsAuthService,
                                                     val lookupService: MtdIdLookupService,
                                                     val service: RetrieveWinterFuelPaymentService,
                                                     val validatorFactory: RetrieveWinterFuelPaymentValidatorFactory,
                                                     val cc: ControllerComponents,
                                                     val idGenerator: IdGenerator)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends AuthorisedController(cc) {

  override val endpointName: String = "retrieve-winter-fuel-payment"

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "RetrieveWinterFuelPaymentController",
      endpointName = "retrieveWinterFuelPayment"
    )

  def retrieve(nino: String, taxYear: String, source: Option[String]): Action[AnyContent] = {
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino, taxYear, source)

      val requestHandler = RequestHandler.withValidator(validator).withService(service.retrieve).withPlainJsonResult()

      requestHandler.handleRequest()
    }
  }

}
