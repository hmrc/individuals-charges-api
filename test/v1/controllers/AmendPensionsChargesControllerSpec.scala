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

import data.AmendPensionChargesData._
import mocks.MockAppConfig
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsJson, Result}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.requestParsers.MockAmendPensionChargesParser
import v1.mocks.services._
import v1.models.errors._
import v1.models.outcomes.DesResponse
import v1.models.requestData.{AmendPensionChargesRawData, AmendPensionChargesRequest, DesTaxYear}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendPensionsChargesControllerSpec extends ControllerBaseSpec
  with MockEnrolmentsAuthService
  with MockMtdIdLookupService
  with MockAmendPensionChargesParser
  with MockAmendPensionsChargesService
  with MockAppConfig
  with MockAuditService
  {

    private val correlationId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"
    private val nino   = "AA123456A"
    private val taxYear = "2021-22"

    private val rawData = AmendPensionChargesRawData(nino, taxYear, AnyContentAsJson(fullJson))
    private val requestData = AmendPensionChargesRequest(Nino(nino),DesTaxYear(taxYear), pensionCharges)

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
         |         "rel":"amend-charges-pensions"
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
        appConfig = mockAppConfig,
        cc = cc
      )

      MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
      MockedEnrolmentsAuthService.authoriseUser()
      MockedAppConfig.apiGatewayContext.returns("individuals/charges").anyNumberOfTimes()
    }

    "amend" should {
      "return a successful response with header X-CorrelationId and body" when {
        "the request received is valid" in new Test {

          MockAmendPensionChargesParser
            .parseRequest(rawData)
            .returns(Right(requestData))

          MockAmendPensionsChargesService
            .amend(requestData)
            .returns(Future.successful(Right(DesResponse(correlationId, Unit))))

          val result: Future[Result] = controller.amend(nino, taxYear)(fakePutRequest(fullJson))
          status(result) shouldBe OK
          contentAsJson(result) shouldBe hateoasResponse
          header("X-CorrelationId", result) shouldBe Some(correlationId)
        }
      }
      "return the error as per the spec" when {
        "parser errors occur" should {
          def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
            s"a ${error.code} error is returned from the parser" in new Test {
              MockAmendPensionChargesParser
                .parseRequest(rawData)
                .returns(Left(ErrorWrapper(Some(correlationId), Seq(error))))

              val result: Future[Result] = controller.amend(nino, taxYear)(fakePutRequest(fullJson))

              status(result) shouldBe expectedStatus
              contentAsJson(result) shouldBe Json.toJson(error)
              header("X-CorrelationId", result) shouldBe Some(correlationId)
            }
          }
          // TODO: update when validator complete
          val input = Seq(
            (BadRequestError, BAD_REQUEST),
            (NinoFormatError, BAD_REQUEST),
            (TaxYearFormatError, BAD_REQUEST),
            (RuleTaxYearRangeInvalid, BAD_REQUEST),
            (RuleTaxYearNotSupportedError, BAD_REQUEST),
            (NotFoundError, NOT_FOUND),
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
                .returns(Future.successful(Left(ErrorWrapper(Some(correlationId), Seq(mtdError)))))

              val result: Future[Result] = controller.amend(nino, taxYear)(fakePutRequest(fullJson))

              status(result) shouldBe expectedStatus
              contentAsJson(result) shouldBe Json.toJson(mtdError)
              header("X-CorrelationId", result) shouldBe Some(correlationId)
            }
          }

          val input = Seq(
            (TaxYearFormatError, BAD_REQUEST),
            (RuleIncorrectOrEmptyBodyError, BAD_REQUEST),
            (NotFoundError, NOT_FOUND),
            (DownstreamError, INTERNAL_SERVER_ERROR)
          )
          input.foreach(args => (serviceErrors _).tupled(args))
        }
      }
    }
}