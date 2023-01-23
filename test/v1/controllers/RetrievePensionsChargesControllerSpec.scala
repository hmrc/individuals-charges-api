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

import api.models.errors.{ErrorWrapper, NinoFormatError, TaxYearFormatError}
import api.models.outcome.ResponseWrapper
import app.controllers.{ControllerBaseSpec, ControllerTestRunner}
import app.mocks.services.MockEnrolmentsAuthService
import mocks.MockAppConfig
import play.api.mvc.Result
import v1.data.RetrievePensionChargesData.{fullJsonWithHateoas, retrieveResponse}
import v1.mocks.MockIdGenerator
import v1.mocks.hateoas.MockHateoasFactory
import v1.mocks.requestParsers.MockRetrievePensionChargesParser
import v1.mocks.services._
import v1.models.domain.Nino
import v1.models.hateoas.Method.{DELETE, GET, PUT}
import v1.models.hateoas.RelType.{AMEND_PENSION_CHARGES, DELETE_PENSION_CHARGES, SELF}
import v1.models.hateoas.{HateoasWrapper, Link}
import v1.models.request.RetrievePensionCharges.{RetrievePensionChargesRawData, RetrievePensionChargesRequest}
import v1.models.request.TaxYear
import v1.models.response.retrieve.RetrievePensionChargesHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrievePensionsChargesControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrievePensionChargesParser
    with MockRetrievePensionsChargesService
    with MockHateoasFactory
    with MockAppConfig
    with MockAuditService
    with MockIdGenerator {

  private val taxYear = "2021-22"

  private val rawData     = RetrievePensionChargesRawData(nino, taxYear)
  private val requestData = RetrievePensionChargesRequest(Nino(nino), TaxYear.fromMtd(taxYear))

  private val retrieveHateoasLink =
    Link(href = s"/individuals/charges/pensions/$nino/$taxYear", method = GET, rel = SELF)

  private val amendHateoasLink =
    Link(href = s"/individuals/charges/pensions/$nino/$taxYear", method = PUT, rel = AMEND_PENSION_CHARGES)

  private val deleteHateoasLink =
    Link(href = s"/individuals/charges/pensions/$nino/$taxYear", method = DELETE, rel = DELETE_PENSION_CHARGES)

  class Test extends ControllerTest {

    val controller = new RetrievePensionChargesController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockRetrievePensionChargesParser,
      service = mockRetrievePensionsChargesService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    override protected def callController(): Future[Result] = controller.retrieve(nino, taxYear)(fakeGetRequest)
  }

  "retrieve" should {
    "return a successful response with header X-CorrelationId and body" when {
      "the request received is valid" in new Test {

        MockRetrievePensionChargesParser
          .parseRequest(rawData)
          .returns(Right(requestData))

        MockRetrievePensionsChargesService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, retrieveResponse))))

        MockHateoasFactory
          .wrap(retrieveResponse, RetrievePensionChargesHateoasData(nino, taxYear))
          .returns(HateoasWrapper(retrieveResponse, links = Seq(retrieveHateoasLink, amendHateoasLink, deleteHateoasLink)))

        val result: Future[Result] = controller.retrieve(nino, taxYear)(fakeRequest)
        status(result) shouldBe OK
        contentAsJson(result) shouldBe fullJsonWithHateoas(taxYear)
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockRetrievePensionChargesParser
          .parseRequest(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        MockRetrievePensionChargesParser
          .parseRequest(rawData)
          .returns(Right(requestData))

        MockRetrievePensionsChargesService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, TaxYearFormatError))))

        runErrorTest(TaxYearFormatError)
      }
    }

  }

}
