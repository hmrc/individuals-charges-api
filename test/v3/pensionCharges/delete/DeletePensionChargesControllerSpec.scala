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

import cats.implicits.catsSyntaxValidatedId
import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.mvc.Result
import shared.config.Deprecation.NotDeprecated
import shared.config.MockSharedAppConfig
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors.{ErrorWrapper, NinoFormatError, TaxYearFormatError}
import shared.models.outcomes.ResponseWrapper
import shared.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v3.pensionCharges.delete.DeletePensionChargesController
import v3.pensionCharges.delete.def1.request.Def1_DeletePensionChargesRequestData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeletePensionChargesControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockDeletePensionChargesValidatorFactory
    with MockDeletePensionChargesService
    with MockAuditService {

  private val taxYear     = "2020-21"
  private val requestData = Def1_DeletePensionChargesRequestData(Nino(validNino), TaxYear.fromMtd(taxYear))

  "delete" should {
    "return a successful response with header X-CorrelationId and body" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockedSharedAppConfig
          .deprecationFor(apiVersion)
          .returns(NotDeprecated.valid)
          .anyNumberOfTimes()

        MockDeletePensionChargesService
          .delete(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        runOkTestWithAudit(expectedStatus = NO_CONTENT)
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockedSharedAppConfig
          .deprecationFor(apiVersion)
          .returns(NotDeprecated.valid)
          .anyNumberOfTimes()

        willUseValidator(returning(NinoFormatError))

        runErrorTestWithAudit(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockedSharedAppConfig
          .deprecationFor(apiVersion)
          .returns(NotDeprecated.valid)
          .anyNumberOfTimes()

        MockDeletePensionChargesService
          .delete(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, TaxYearFormatError))))

        runErrorTestWithAudit(TaxYearFormatError)
      }
    }
  }

  class Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] with MockSharedAppConfig {

    val controller: DeletePensionChargesController = new DeletePensionChargesController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      service = mockDeleteBFLossService,
      validatorFactory = mockDeletePensionChargesValidatorFactory,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false
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
          maybeRequestBody,
          correlationId,
          auditResponse
        )
      )

  }

}
