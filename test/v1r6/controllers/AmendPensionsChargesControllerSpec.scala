/*
 * Copyright 2021 HM Revenue & Customs
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

package v1r6.controllers

import v1r6.data.AmendPensionChargesData._
import mocks.MockAppConfig
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsJson, Result}
import uk.gov.hmrc.http.HeaderCarrier
import v1r6.mocks.MockIdGenerator
import v1r6.mocks.hateoas.MockHateoasFactory
import v1r6.mocks.requestParsers.MockAmendPensionChargesParser
import v1r6.mocks.services._
import v1r6.models.audit.{AuditError, AuditEvent, AuditResponse, GenericAuditDetail}
import v1r6.models.domain.Nino
import v1r6.models.errors._
import v1r6.models.hateoas.Method.{DELETE, GET, PUT}
import v1r6.models.hateoas.{HateoasWrapper, Link}
import v1r6.models.outcomes.ResponseWrapper
import v1r6.models.request.AmendPensionCharges.{AmendPensionChargesRawData, AmendPensionChargesRequest}
import v1r6.models.request.DesTaxYear
import v1r6.models.response.amend.AmendPensionChargesHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendPensionsChargesControllerSpec extends ControllerBaseSpec
  with MockEnrolmentsAuthService
  with MockMtdIdLookupService
  with MockAmendPensionChargesParser
  with MockAmendPensionsChargesService
  with MockAppConfig
  with MockAuditService
  with MockHateoasFactory
  with MockIdGenerator {

  private val correlationId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"
  private val nino = "AA123456A"
  private val taxYear = "2021-22"
  private val rawData = AmendPensionChargesRawData(nino, taxYear, AnyContentAsJson(fullJson))
  private val requestData = AmendPensionChargesRequest(Nino(nino), DesTaxYear(taxYear), pensionCharges)
  private val testHateoasLinks = Seq(
    Link(href = s"/individuals/charges/pensions/$nino/$taxYear", method = GET, rel = "self"),
    Link(href = s"/individuals/charges/pensions/$nino/$taxYear", method = PUT, rel = "create-and-amend-charges-pensions"),
    Link(href = s"/individuals/charges/pensions/$nino/$taxYear", method = DELETE, rel = "delete-charges-pensions")
  )

  val hateoasResponse: JsValue = Json.parse(
    s"""
       |{
       |   "links":[
       |      {
       |         "href":"/individuals/charges/pensions/$nino/$taxYear",
       |         "method":"GET",
       |         "rel":"self"
       |      },
       |      {
       |         "href":"/individuals/charges/pensions/$nino/$taxYear",
       |         "method":"PUT",
       |         "rel":"create-and-amend-charges-pensions"
       |      },
       |      {
       |         "href":"/individuals/charges/pensions/$nino/$taxYear",
       |         "method":"DELETE",
       |         "rel":"delete-charges-pensions"
       |      }
       |   ]
       |}
    """.stripMargin
  )

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new AmendPensionChargesController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockAmendPensionChargesParser,
      service = mockAmendPensionsChargesService,
      auditService = mockAuditService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockAppConfig.apiGatewayContext.returns("individuals/charges").anyNumberOfTimes()
    MockIdGenerator.generateCorrelationId.returns(correlationId)

  }

  def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
    AuditEvent(
      auditType = "CreateAmendPensionsCharges",
      transactionName = "create-amend-pensions-charges",
      detail = GenericAuditDetail(
        userType = "Individual",
        agentReferenceNumber = None,
        params = Map("nino" -> nino, "taxYear" -> taxYear),
        request = requestBody,
        `X-CorrelationId` = correlationId,
        response = auditResponse)
    )

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
          .wrap((), AmendPensionChargesHateoasData(nino, taxYear))
          .returns(HateoasWrapper((), testHateoasLinks))

        val result: Future[Result] = controller.amend(nino, taxYear)(fakePutRequest(fullJson))
        status(result) shouldBe OK
        contentAsJson(result) shouldBe hateoasResponse
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(OK, None, Some(hateoasResponse))
        MockedAuditService.verifyAuditEvent(event(auditResponse, Some(fullJson))).once
      }
    }

    "return the error as per the spec" when {
      "parser errors occur" should {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {
            MockAmendPensionChargesParser
              .parseRequest(rawData)
              .returns(Left(ErrorWrapper(correlationId, error)))

            val result: Future[Result] = controller.amend(nino, taxYear)(fakePutRequest(fullJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(error.code))), None)
            MockedAuditService.verifyAuditEvent(event(auditResponse, Some(fullJson))).once
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (RuleTaxYearRangeInvalid, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (ValueFormatError, BAD_REQUEST),
          (RuleCountryCodeError, BAD_REQUEST),
          (CountryCodeFormatError, BAD_REQUEST),
          (PensionSchemeTaxRefFormatError, BAD_REQUEST),
          (ProviderAddressFormatError, BAD_REQUEST),
          (QOPSRefFormatError, BAD_REQUEST),
          (ProviderNameFormatError, BAD_REQUEST),
          (RuleIsAnnualAllowanceReducedError, BAD_REQUEST),
          (RuleBenefitExcessesError, BAD_REQUEST),
          (RulePensionReferenceError, BAD_REQUEST),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )
        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" should {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockAmendPensionChargesParser
              .parseRequest(rawData)
              .returns(Right(requestData))

            MockAmendPensionsChargesService
              .amend(requestData)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.amend(nino, taxYear)(fakePutRequest(fullJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(mtdError.code))), None)
            MockedAuditService.verifyAuditEvent(event(auditResponse, Some(fullJson))).once
          }
        }

        val input = Seq(
          (TaxYearFormatError, BAD_REQUEST),
          (RuleIncorrectOrEmptyBodyError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )
        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}
