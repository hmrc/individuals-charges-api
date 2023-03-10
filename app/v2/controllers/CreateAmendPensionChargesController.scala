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

package v2.controllers

import api.controllers._
import api.hateoas.HateoasFactory
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContentAsJson, ControllerComponents}
import utils.IdGenerator
import v2.controllers.requestParsers.CreateAmendPensionChargesParser
import v2.models.request.createAmendPensionCharges._
import v2.models.response.createAmendPensionCharges.CreateAmendPensionChargesHateoasData
import v2.models.response.createAmendPensionCharges.CreateAmendPensionChargesResponse.CreateAmendLinksFactory
import v2.services.CreateAmendPensionChargesService

import javax.inject._
import scala.concurrent.ExecutionContext

@Singleton
class CreateAmendPensionChargesController @Inject() (val authService: EnrolmentsAuthService,
                                                     val lookupService: MtdIdLookupService,
                                                     service: CreateAmendPensionChargesService,
                                                     parser: CreateAmendPensionChargesParser,
                                                     hateoasFactory: HateoasFactory,
                                                     auditService: AuditService,
                                                     cc: ControllerComponents,
                                                     val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc) {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "CreateAmendPensionChargesController", endpointName = "Create & Amend a Pensions Charge")

  def createAmend(nino: String, taxYear: String): Action[JsValue] = {
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val rawData = CreateAmendPensionChargesRawData(nino, taxYear, AnyContentAsJson(request.body))

      val requestHandler =
        RequestHandler
          .withParser(parser)
          .withService(service.createAmendPensions)
          .withHateoasResult(hateoasFactory)(CreateAmendPensionChargesHateoasData(nino, taxYear))
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