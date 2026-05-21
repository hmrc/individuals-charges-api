/*
 * Copyright 2025 HM Revenue & Customs
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

package v3.winterFuelPayment.createAmend

import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.mvc.Result
import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.TaxYear
import api.models.errors.*
import api.models.outcomes.ResponseWrapper
import v3.winterFuelPayment.createAmend.fixture.CreateAmendWinterFuelPaymentFixtures.{requestBodyModel, validRequestBodyJson}
import v3.winterFuelPayment.createAmend.models.request.CreateAmendWinterFuelPaymentRequestData
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendWinterFuelPaymentControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockCreateAmendWinterFuelPaymentService
    with MockCreateAmendWinterFuelPaymentValidatorFactory {

  private val taxYear: String = "2026-27"

  private val requestData: CreateAmendWinterFuelPaymentRequestData = CreateAmendWinterFuelPaymentRequestData(
    nino = parsedNino,
    taxYear = TaxYear.fromMtd(taxYear),
    body = requestBodyModel
  )

  "CreateAmendWinterFuelPaymentController" should {
    "return a successful response with status 204 (NO_CONTENT)" when {
      "given a valid request" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockCreateAmendWinterFuelPaymentService
          .createAmend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        runOkTestWithAudit(expectedStatus = NO_CONTENT, maybeAuditRequestBody = Some(validRequestBodyJson))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTestWithAudit(NinoFormatError, Some(validRequestBodyJson))
      }

      "service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockCreateAmendWinterFuelPaymentService
          .createAmend(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, TaxYearFormatError))))

        runErrorTestWithAudit(TaxYearFormatError, Some(validRequestBodyJson))
      }
    }
  }

  private trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller: CreateAmendWinterFuelPaymentController = new CreateAmendWinterFuelPaymentController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockCreateAmendWinterFuelPaymentValidatorFactory,
      service = mockCreateAmendWinterFuelPaymentService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.createAmend(validNino, taxYear)(fakePostRequest(validRequestBodyJson))

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "CreateAmendWinterFuelPayment",
        transactionName = "create-amend-winter-fuel-payment",
        detail = GenericAuditDetail(
          userType = "Individual",
          versionNumber = apiVersion.name,
          agentReferenceNumber = None,
          params = Map("nino" -> validNino, "taxYear" -> taxYear),
          `X-CorrelationId` = correlationId,
          requestBody = Some(validRequestBodyJson),
          auditResponse = auditResponse
        )
      )

  }

}
