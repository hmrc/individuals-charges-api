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

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.mocks.MockIdGenerator
import api.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import api.models.errors.{ErrorWrapper, NinoFormatError, TaxYearFormatError}
import api.models.outcome.ResponseWrapper
import play.api.mvc.Result
import v1.mocks.requestParsers.MockDeletePensionChargesParser
import v1.mocks.services.MockDeletePensionChargesService
import v1.models.request.DeletePensionCharges.{DeletePensionChargesRawData, DeletePensionChargesRequest}
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.{Nino, TaxYear}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeletePensionChargesControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockDeletePensionChargesService
    with MockDeletePensionChargesParser
    with MockAuditService
    with MockIdGenerator {

  val taxYear = "2020-21"

  val rawData = DeletePensionChargesRawData(nino, taxYear)
  val request = DeletePensionChargesRequest(Nino(nino), TaxYear.fromMtd(taxYear))

  class Test extends ControllerTest {

    val controller = new DeletePensionChargesController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      service = mockDeleteBFLossService,
      parser = mockDeletePensionChargesParser,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    override protected def callController(): Future[Result] = controller.delete(nino, taxYear)(fakeRequest)

    def event(auditResponse: AuditResponse): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "DeletePensionsCharges",
        transactionName = "delete-pensions-charges",
        detail = GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          params = Map("nino" -> nino, "taxYear" -> taxYear),
          request = None,
          `X-CorrelationId` = correlationId,
          response = auditResponse
        )
      )

  }

  "delete" should {
    "return a successful response with header X-CorrelationId and body" when {
      "the request received is valid" in new Test {

        MockDeletePensionChargesParser.parseRequest(rawData).returns(Right(request))

        MockDeletePensionChargesService
          .delete(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        val result: Future[Result] = controller.delete(nino, taxYear)(fakeRequest)
        status(result) shouldBe NO_CONTENT
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(NO_CONTENT, None, None)
        MockedAuditService.verifyAuditEvent(event(auditResponse)).once
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockDeletePensionChargesParser
          .parseRequest(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        MockDeletePensionChargesParser
          .parseRequest(rawData)
          .returns(Right(request))

        MockDeletePensionChargesService
          .delete(request)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, TaxYearFormatError))))

        runErrorTest(TaxYearFormatError)
      }
    }
  }

}
