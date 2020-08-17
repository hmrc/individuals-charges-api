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

import play.api.mvc.Result
import data.RetrievePensionChargesData.{fullJson, fullJsonWithHateoas, retrieveResponse}
import mocks.MockAppConfig
import play.api.libs.json.Json
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.hateoas.MockHateoasFactory
import v1.mocks.requestParsers.MockRetrievePensionChargesParser
import v1.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService, MockRetrievePensionsChargesService}
import v1.models.des.RetrievePensionChargesHateoasData
import v1.models.errors._
import v1.models.hateoas.{HateoasWrapper, Link}
import v1.models.hateoas.Method.{DELETE, GET, PUT}
import v1.models.hateoas.RelType.{AMEND_PENSION_CHARGES, DELETE_PENSION_CHARGES, SELF}
import v1.models.outcomes.DesResponse
import v1.models.requestData.{DesTaxYear, RetrievePensionChargesRawData, RetrievePensionChargesRequest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrievePensionsChargesControllerSpec extends ControllerBaseSpec
  with MockEnrolmentsAuthService
  with MockMtdIdLookupService
  with MockRetrievePensionChargesParser
  with MockRetrievePensionsChargesService
  with MockHateoasFactory
  with MockAppConfig
  with MockAuditService
  {

    private val correlationId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"
    private val nino   = "AA123456A"
    private val taxYear = "2020-21"

    private val rawData = RetrievePensionChargesRawData(nino, taxYear)
    private val requestData = RetrievePensionChargesRequest(Nino(nino),DesTaxYear(taxYear))

    private val retrieveHateoasLink = Link(href = s"individuals/charges/pensions/$nino/$taxYear",method = GET, rel = SELF)
    private val amendHateoasLink = Link(href = s"individuals/charges/pensions/$nino/$taxYear", method = PUT, rel = AMEND_PENSION_CHARGES)
    private val deleteHateoasLink = Link(href = s"individuals/charges/pensions/$nino/$taxYear", method = DELETE, rel = DELETE_PENSION_CHARGES)

    trait Test {
      val hc = HeaderCarrier()

      val controller = new RetrievePensionChargesController(
        authService = mockEnrolmentsAuthService,
        lookupService = mockMtdIdLookupService,
        requestParser = mockRetrievePensionChargesParser,
        service = mockRetrievePensionsChargesService,
        hateoasFactory = mockHateoasFactory,
        auditService = mockAuditService,
        cc = cc
      )

      MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
      MockedEnrolmentsAuthService.authoriseUser()
    }

    "retrieve" should {
      "return a successful response with header X-CorrelationId and body" when {
        "the request received is valid" in new Test {

          MockRetrievePensionChargesParser
            .parseRequest(rawData)
            .returns(Right(requestData))

          MockRetrievePensionsChargesService
            .retrieve(requestData)
            .returns(Future.successful(Right(DesResponse(correlationId, retrieveResponse))))

          MockHateoasFactory
            .wrap(retrieveResponse, RetrievePensionChargesHateoasData(nino,taxYear))
            .returns(HateoasWrapper(retrieveResponse,links = Seq(retrieveHateoasLink, amendHateoasLink,deleteHateoasLink)))

          val result: Future[Result] = controller.retrieve(nino, taxYear)(fakeRequest)
          status(result) shouldBe OK
          contentAsJson(result) shouldBe fullJsonWithHateoas
          header("X-CorrelationId", result) shouldBe Some(correlationId)
        }
      }
      "return the error as per the spec" when {
        "parser errors occur" should {
          def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {
            MockRetrievePensionChargesParser
              .parseRequest(rawData)
              .returns(Left(ErrorWrapper(Some(correlationId), Seq(error))))

            val result: Future[Result] = controller.retrieve(nino, taxYear)(fakeRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
          }
          val input = Seq(
            (BadRequestError, BAD_REQUEST),
            (NinoFormatError, BAD_REQUEST),
            (TaxYearFormatError, BAD_REQUEST)
          )
          input.foreach(args => (errorsFromParserTester _).tupled(args))
        }
        "service errors occur" should {
          def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
            s"a $mtdError error is returned from the service" in new Test {

              MockRetrievePensionChargesParser
                .parseRequest(rawData)
                .returns(Right(requestData))

              MockRetrievePensionsChargesService
                .retrieve(requestData)
                .returns(Future.successful(Left(ErrorWrapper(Some(correlationId), Seq(mtdError)))))

              val result: Future[Result] = controller.retrieve(nino, taxYear)(fakeRequest)

              status(result) shouldBe expectedStatus
              contentAsJson(result) shouldBe Json.toJson(mtdError)
              header("X-CorrelationId", result) shouldBe Some(correlationId)
            }
          }

          val input = Seq(
            (NinoFormatError, BAD_REQUEST),
            (TaxYearFormatError, BAD_REQUEST),
            (NotFoundError, NOT_FOUND),
            (DownstreamError, INTERNAL_SERVER_ERROR)
          )

          input.foreach(args => (serviceErrors _).tupled(args))
        }
      }
    }
}
