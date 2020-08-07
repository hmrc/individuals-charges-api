/*
 * Copyright 2020 HM Revenue & Customs
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

import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.auth.core.Enrolment
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.requestParsers.MockDeletePensionChargesParser
import v1.mocks.services.{MockAuditService, MockDeletePensionChargesService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v1.models.audit.{AuditError, AuditEvent, AuditResponse, GenericAuditDetail}
import v1.models.errors.{NotFoundError, _}
import v1.models.outcomes.DesResponse
import v1.models.requestData.{DeletePensionChargesRawData, DeletePensionChargesRequest, DesTaxYear}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeletePensionChargesControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockDeletePensionChargesService
    with MockDeletePensionChargesParser
    with MockAuditService {

  val correlationId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  val nino   = "AA123456A"
  val taxYear = "2020-21"

  val rawData = DeletePensionChargesRawData(nino, taxYear)
  val request = DeletePensionChargesRequest(Nino(nino), DesTaxYear(taxYear))

  trait Test {
    val hc = HeaderCarrier()

    val controller = new DeletePensionChargesController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      service = mockDeleteBFLossService,
      requestParser = mockDeletePensionChargesParser,
      auditService = mockAuditService,
      cc = cc
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
  }

  "delete" should {
    "return a successful response with header X-CorrelationId and body" when {
      "the request received is valid" in new Test {

        MockDeletePensionChargesParser.parseRequest(rawData).returns(Right(request))

        MockDeletePensionChargesService.delete(request).returns(Future.successful(Right(DesResponse(correlationId, ()))))

        val result: Future[Result] = controller.delete(nino, taxYear)(fakeRequest)
        status(result) shouldBe NO_CONTENT
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val detail = GenericAuditDetail("Individual", None, rawData.nino, correlationId, AuditResponse(NO_CONTENT, None, None))
        val event = AuditEvent("deletePensionChargesAuditType", "delete-pension-charges-transaction-type", detail)
        MockedAuditService.verifyAuditEvent(event).once
      }
    }

    "handle mdtp validation errors as per spec" when {
      def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
        s"a ${error.code} error is returned from the parser" in new Test {

          MockDeletePensionChargesParser.parseRequest(rawData).returns(Left(ErrorWrapper(Some(correlationId), Seq(error))))

          val response: Future[Result] = controller.delete(nino, taxYear)(fakeRequest)

          status(response) shouldBe expectedStatus
          contentAsJson(response) shouldBe Json.toJson(error)
          header("X-CorrelationId", response) shouldBe Some(correlationId)

          val detail = GenericAuditDetail("Individual", None, rawData.nino, correlationId,
            AuditResponse(expectedStatus, Some(Seq(AuditError(error.code))), None))
          val event = AuditEvent("deletePensionChargesAuditType", "delete-pension-charges-transaction-type", detail)
          MockedAuditService.verifyAuditEvent(event).once
        }
      }

      errorsFromParserTester(NinoFormatError, BAD_REQUEST)
      errorsFromParserTester(TaxYearFormatError, BAD_REQUEST)
      errorsFromParserTester(BadRequestError, BAD_REQUEST)
      //TODO ADD IN ERRORS WHEN VALIDATION IS IN
//      errorsFromParserTester(RULE_TAX_YEAR_RANGE_INVALID, BAD_REQUEST)
//      errorsFromParserTester(RULE_TAX_YEAR_NOT_SUPPORTED, BAD_REQUEST)
      errorsFromParserTester(NotFoundError, NOT_FOUND)
      errorsFromParserTester(DownstreamError, INTERNAL_SERVER_ERROR)
    }

    "handle non-mdtp validation errors as per spec" when {
      def errorsFromServiceTester(error: MtdError, expectedStatus: Int): Unit = {
        s"a ${error.code} error is returned from the service" in new Test {

          MockDeletePensionChargesParser.parseRequest(rawData).returns(Right(request))

          MockDeletePensionChargesService.delete(request).returns(Future.successful(Left(ErrorWrapper(Some(correlationId), Seq(error)))))

          val response: Future[Result] = controller.delete(nino, taxYear)(fakeRequest)
          status(response) shouldBe expectedStatus
          contentAsJson(response) shouldBe Json.toJson(error)
          header("X-CorrelationId", response) shouldBe Some(correlationId)

          val detail = GenericAuditDetail("Individual", None, rawData.nino, correlationId,
            AuditResponse(expectedStatus, Some(Seq(AuditError(error.code))), None))
          val event = AuditEvent("deletePensionChargesAuditType", "delete-pension-charges-transaction-type", detail)
          MockedAuditService.verifyAuditEvent(event).once
        }
      }

      errorsFromServiceTester(NinoFormatError, BAD_REQUEST)
      errorsFromServiceTester(TaxYearFormatError, BAD_REQUEST)
      errorsFromServiceTester(BadRequestError, BAD_REQUEST)
      //TODO ADD IN ERRORS WHEN VALIDATION IS IN
      //      errorsFromServiceTester(RULE_TAX_YEAR_RANGE_INVALID, BAD_REQUEST)
      //      errorsFromServiceTester(RULE_TAX_YEAR_NOT_SUPPORTED, BAD_REQUEST)
      errorsFromServiceTester(NotFoundError, NOT_FOUND)
      errorsFromServiceTester(DownstreamError, INTERNAL_SERVER_ERROR)
    }
  }
}
