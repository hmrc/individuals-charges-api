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

package v1.connectors

import api.connectors.ConnectorSpec
import api.models.domain.{Nino, TaxYear}
import api.models.errors.{InternalError, NinoFormatError, TaxYearFormatError}
import api.models.outcomes.ResponseWrapper
import v1.data.AmendPensionChargesData.pensionCharges
import v1.models.request.AmendPensionCharges.AmendPensionChargesRequest

import scala.concurrent.Future

class AmendPensionChargesConnectorSpec extends ConnectorSpec {

  val nino = "AA123456A"

  trait Test {
    _: ConnectorTest =>

    def taxYear: TaxYear

    protected val request: AmendPensionChargesRequest = AmendPensionChargesRequest(
      nino = Nino(nino),
      taxYear = taxYear,
      pensionCharges = pensionCharges
    )

    val connector: AmendPensionChargesConnector =
      new AmendPensionChargesConnector(http = mockHttpClient, appConfig = mockAppConfig)

  }

  "Amend pension charges" when {

    "a valid request is supplied" should {
      "return a successful response with the correct correlationId" in new IfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

        val expected = Right(ResponseWrapper(correlationId, Unit))

        MockedHttpClient
          .put(
            url = s"$baseUrl/income-tax/charges/pensions/$nino/${taxYear.asMtd}",
            body = pensionCharges,
            config = dummyIfsHeaderCarrierConfig,
            requiredHeaders = requiredIfsHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(expected))

        await(connector.amendPensionCharges(request)) shouldBe expected
      }
    }

    "a valid request is supplied for a Tax Year Specific tax year" should {
      "return a successful response with the correct correlationId" in new TysIfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

        val expected = Right(ResponseWrapper(correlationId, Unit))

        MockedHttpClient
          .put(
            url = s"$baseUrl/income-tax/charges/pensions/23-24/$nino",
            body = pensionCharges,
            config = dummyIfsHeaderCarrierConfig,
            requiredHeaders = requiredTysIfsHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(expected))

        await(connector.amendPensionCharges(request)) shouldBe expected
      }
    }

    "A request returning a single error" should {
      "return an unsuccessful response with the correct correlationId and a single error" in new IfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

        val expected = Left(ResponseWrapper(correlationId, NinoFormatError))

        MockedHttpClient
          .put(
            url = s"$baseUrl/income-tax/charges/pensions/$nino/${taxYear.asMtd}",
            body = pensionCharges,
            config = dummyIfsHeaderCarrierConfig,
            requiredHeaders = requiredIfsHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(expected))

        await(connector.amendPensionCharges(request)) shouldBe expected
      }
    }

    "a request returning multiple errors" should {
      "return an unsuccessful response with the correct correlationId and multiple errors" in new IfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

        val expected = Left(ResponseWrapper(correlationId, Seq(NinoFormatError, InternalError, TaxYearFormatError)))

        MockedHttpClient
          .put(
            url = s"$baseUrl/income-tax/charges/pensions/$nino/${taxYear.asMtd}",
            body = pensionCharges,
            config = dummyIfsHeaderCarrierConfig,
            requiredHeaders = requiredIfsHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(expected))

        await(connector.amendPensionCharges(request)) shouldBe expected
      }
    }
  }

}