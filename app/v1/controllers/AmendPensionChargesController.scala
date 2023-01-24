/*
 * Copyright 2023 HM Revenue & Customs
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

import api.controllers._
import api.hateoas.HateoasFactory
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContentAsJson, ControllerComponents}
import utils.IdGenerator
import v1.controllers.requestParsers.AmendPensionChargesParser
import v1.models.request.AmendPensionCharges
import v1.models.response.amend.AmendPensionChargesHateoasData
import v1.models.response.amend.AmendPensionChargesResponse.AmendLinksFactory
import v1.services._

import javax.inject._
import scala.concurrent.ExecutionContext

@Singleton
class AmendPensionChargesController @Inject() (val authService: EnrolmentsAuthService,
                                               val lookupService: MtdIdLookupService,
                                               service: AmendPensionChargesService,
                                               parser: AmendPensionChargesParser,
                                               hateoasFactory: HateoasFactory,
                                               auditService: AuditService,
                                               cc: ControllerComponents,
                                               val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with BaseController {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "AmendPensionChargesController", endpointName = "Amend a Pensions Charge")

  def amend(nino: String, taxYear: String): Action[JsValue] = {
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val rawData = AmendPensionCharges.AmendPensionChargesRawData(nino, taxYear, AnyContentAsJson(request.body))

      val requestHandler =
        RequestHandler
          .withParser(parser)
          .withService(service.amendPensions)
          .withHateoasResult(hateoasFactory)(AmendPensionChargesHateoasData(nino, taxYear))
          .withAuditing(AuditHandler(
            auditService,
            auditType = "CreateAmendPensionsCharges",
            transactionName = "create-amend-pensions-charges",
            params = Map("nino" -> nino, "taxYear" -> taxYear),
            Some(request.body),
            includeResponse = true
          ))

      requestHandler.handleRequest(rawData)
    }
  }

}
