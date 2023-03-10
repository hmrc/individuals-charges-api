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

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.mocks.MockIdGenerator
import api.mocks.hateoas.MockHateoasFactory
import api.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.{Nino, TaxYear}
import api.models.errors.{ErrorWrapper, NinoFormatError, RuleIncorrectOrEmptyBodyError}
import api.models.hateoas.HateoasWrapper
import api.models.outcomes.ResponseWrapper
import mocks.MockAppConfig
import play.api.libs.json.JsValue
import play.api.mvc.{AnyContentAsJson, Result}
import v2.data.CreateAmendPensionChargesData._
import v2.mocks.requestParsers.MockCreateAmendPensionChargesParser
import v2.mocks.services._
import v2.models.request.createAmendPensionCharges.{CreateAmendPensionChargesRawData, CreateAmendPensionChargesRequest}
import v2.models.response.createAmendPensionCharges.CreateAmendPensionChargesHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendPensionsChargesControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockCreateAmendPensionChargesParser
    with MockCreateAmendPensionsChargesService
    with MockAppConfig
    with MockAuditService
    with MockHateoasFactory
    with MockIdGenerator {

  private val taxYear     = "2021-22"
  private val rawData     = CreateAmendPensionChargesRawData(nino, taxYear, AnyContentAsJson(fullJson))
  private val requestData = CreateAmendPensionChargesRequest(Nino(nino), TaxYear.fromMtd(taxYear), pensionCharges)

  class Test extends ControllerTest with AuditEventChecking {

    val controller = new CreateAmendPensionChargesController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockCreateAmendPensionChargesParser,
      service = mockCreateAmendPensionsChargesService,
      auditService = mockAuditService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    override protected def callController(): Future[Result] = controller.amend(nino, taxYear)(fakePostRequest(fullJson))

    override protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "CreateAmendPensionsCharges",
        transactionName = "create-amend-pensions-charges",
        detail = GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          params = Map("nino" -> nino, "taxYear" -> taxYear),
          maybeRequestBody,
          correlationId,
          auditResponse
        )
      )

  }

  "amend" should {
    "return a successful response with header X-CorrelationId and body" when {
      "the request received is valid" in new Test {

        MockAmendPensionChargesParser
          .parseRequest(rawData)
          .returns(Right(requestData))

        MockAmendPensionsChargesService
          .amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), CreateAmendPensionChargesHateoasData(nino, taxYear))
          .returns(HateoasWrapper((), hateoaslinks))

        runOkTestWithAudit(
          expectedStatus = OK,
          maybeExpectedResponseBody = Some(hateoaslinksJson),
          maybeAuditRequestBody = Some(fullJson),
          maybeAuditResponseBody = Some(hateoaslinksJson))

      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockAmendPensionChargesParser
          .parseRequest(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTestWithAudit(NinoFormatError, maybeAuditRequestBody = Some(fullJson))
      }

      "the service returns an error" in new Test {
        MockAmendPensionChargesParser
          .parseRequest(rawData)
          .returns(Right(requestData))

        MockAmendPensionsChargesService
          .amend(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))))

        runErrorTestWithAudit(RuleIncorrectOrEmptyBodyError, maybeAuditRequestBody = Some(fullJson))
      }
    }
  }

}
