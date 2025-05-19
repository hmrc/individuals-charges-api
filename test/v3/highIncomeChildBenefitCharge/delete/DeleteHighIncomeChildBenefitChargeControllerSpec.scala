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

package v3.highIncomeChildBenefitCharge.delete

import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.mvc.Result
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import shared.models.domain.TaxYear
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import v3.highIncomeChildBenefitCharge.delete.model.request.DeleteHighIncomeChildBenefitChargeRequestData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteHighIncomeChildBenefitChargeControllerSpec
  extends ControllerBaseSpec
    with ControllerTestRunner
    with MockDeleteHighIncomeChildBenefitChargeService
    with MockDeleteHighIncomeChildBenefitChargeValidatorFactory {

  private val taxYear: String      = "2025-26"

  private val requestData: DeleteHighIncomeChildBenefitChargeRequestData = DeleteHighIncomeChildBenefitChargeRequestData(
    nino = parsedNino,
    taxYear = TaxYear.fromMtd(taxYear)
  )

  "DeleteHighIncomeChildBenefitChargeController" should {
    "return a successful response with status 204 (NO_CONTENT)" when {
      "given a valid request" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockDeleteHighIncomeChildBenefitChargeService
          .delete(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        runOkTestWithAudit(expectedStatus = NO_CONTENT)
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTestWithAudit(NinoFormatError)
      }

      "service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockDeleteHighIncomeChildBenefitChargeService
          .delete(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, TaxYearFormatError))))

        runErrorTestWithAudit(TaxYearFormatError)
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller: DeleteHighIncomeChildBenefitChargeController = new DeleteHighIncomeChildBenefitChargeController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockDeleteHighIncomeChildBenefitChargeValidatorFactory,
      service = mockDeleteHighIncomeChildBenefitChargeService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.delete(validNino, taxYear)(fakeRequest)

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "DeleteHighIncomeChildBenefitCharge",
        transactionName = "delete-high-income-child-benefit-charge-submission",
        detail = GenericAuditDetail(
          userType = "Individual",
          versionNumber = apiVersion.name,
          agentReferenceNumber = None,
          params = Map("nino" -> validNino, "taxYear" -> taxYear),
          `X-CorrelationId` = correlationId,
          requestBody = None,
          auditResponse = auditResponse
        )
      )

  }

}
