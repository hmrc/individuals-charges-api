/*
 * Copyright 2024 HM Revenue & Customs
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

package v3.pensionCharges.delete

import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.mvc.Result
import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.{Nino, TaxYear}
import api.models.errors.{ErrorWrapper, NinoFormatError, TaxYearFormatError}
import api.models.outcomes.ResponseWrapper
import v3.pensionCharges.delete.def1.request.Def1_DeletePensionChargesRequestData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeletePensionChargesControllerSpec
    extends ControllerTestRunner
    with MockDeletePensionChargesValidatorFactory
    with MockDeletePensionChargesService {

  private val taxYear     = "2020-21"
  private val requestData = Def1_DeletePensionChargesRequestData(Nino(validNino), TaxYear.fromMtd(taxYear))

  "delete" should {
    "return a successful response with status 204 (NO_CONTENT)" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockDeletePensionChargesService
          .deletePensionCharges(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        runOkTestWithAudit(expectedStatus = NO_CONTENT)
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTestWithAudit(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockDeletePensionChargesService
          .deletePensionCharges(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, TaxYearFormatError))))

        runErrorTestWithAudit(TaxYearFormatError)
      }
    }
  }

  class Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller: DeletePensionChargesController = new DeletePensionChargesController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      service = mockDeletePensionChargesService,
      validatorFactory = mockDeletePensionChargesValidatorFactory,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    override protected def callController(): Future[Result] = controller.delete(validNino, taxYear)(fakeRequest)

    override protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "DeletePensionsCharges",
        transactionName = "delete-pensions-charges",
        detail = GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          versionNumber = apiVersion.name,
          params = Map("nino" -> validNino, "taxYear" -> taxYear),
          requestBody = maybeRequestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

  }

}
