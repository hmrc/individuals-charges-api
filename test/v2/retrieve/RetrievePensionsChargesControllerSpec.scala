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

package v2.retrieve

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.{Nino, TaxYear}
import api.models.errors.{ErrorWrapper, NinoFormatError, TaxYearFormatError}
import api.models.outcomes.ResponseWrapper
import api.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import mocks.MockAppConfig
import play.api.Configuration
import play.api.libs.json.{JsObject, JsValue}
import play.api.mvc.Result
import v2.retrieve.def1.model.request.Def1_RetrievePensionChargesRequestData
import v2.retrieve.def1.fixture.RetrievePensionChargesFixture.{fullJson, retrieveResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrievePensionsChargesControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrievePensionChargesValidatorFactory
    with MockRetrievePensionsChargesService
    with MockAppConfig
    with MockAuditService {

  private val taxYear     = "2021-22"
  private val requestData = Def1_RetrievePensionChargesRequestData(Nino(nino), TaxYear.fromMtd(taxYear))

  "retrieve" should {
    "return a successful response with header X-CorrelationId and body" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrievePensionsChargesService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, retrieveResponse))))

        runOkTest(OK, Some(fullJson.as[JsObject]))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrievePensionsChargesService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, TaxYearFormatError))))

        runErrorTest(TaxYearFormatError)
      }
    }

  }

  class Test extends ControllerTest with AuditEventChecking {

    val controller = new RetrievePensionChargesController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrievePensionChargesValidatorFactory,
      service = mockRetrievePensionsChargesService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false
    override protected def callController(): Future[Result] = controller.retrieve(nino, taxYear)(fakeRequest)

    override protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "RetrievePensionsCharges",
        transactionName = "retrieve-pensions-charges",
        detail = GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          versionNumber = "2.0",
          params = Map("nino" -> nino, "taxYear" -> taxYear),
          requestBody = None,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

  }

}
