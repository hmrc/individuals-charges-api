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

package v2.delete

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.{Nino, TaxYear}
import api.models.errors.{ErrorWrapper, NinoFormatError, TaxYearFormatError}
import api.models.outcomes.ResponseWrapper
import api.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import cats.implicits.catsSyntaxValidatedId
import config.Deprecation.NotDeprecated
import mocks.MockIndividualsChargesConfig
import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.mvc.Result
import v2.delete.def1.request.Def1_DeletePensionChargesRequestData

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
  private val requestData = Def1_DeletePensionChargesRequestData(Nino(nino), TaxYear.fromMtd(taxYear))

  "delete" should {
    "return a successful response with header X-CorrelationId and body" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockedIndividualsChargesConfig
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
        MockedIndividualsChargesConfig
          .deprecationFor(apiVersion)
          .returns(NotDeprecated.valid)
          .anyNumberOfTimes()

        willUseValidator(returning(NinoFormatError))

        runErrorTestWithAudit(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockedIndividualsChargesConfig
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

  class Test extends ControllerTest with AuditEventChecking with MockIndividualsChargesConfig {

    val controller = new DeletePensionChargesController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      service = mockDeleteBFLossService,
      validatorFactory = mockDeletePensionChargesValidatorFactory,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedIndividualsChargesConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedIndividualsChargesConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false
    override protected def callController(): Future[Result] = controller.delete(nino, taxYear)(fakeRequest)

    override protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "DeletePensionsCharges",
        transactionName = "delete-pensions-charges",
        detail = GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          versionNumber = "2.0",
          params = Map("nino" -> nino, "taxYear" -> taxYear),
          maybeRequestBody,
          correlationId,
          auditResponse
        )
      )

  }

}
