/*
 * Copyright 2022 HM Revenue & Customs
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

import mocks.{MockAppConfig, MockHttpClient}
import v1.data.AmendPensionChargesData.pensionCharges
import v1.data.RetrievePensionChargesData.retrieveResponse
import v1.models.domain.Nino
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.AmendPensionCharges.AmendPensionChargesRequest
import v1.models.request.DeletePensionCharges.DeletePensionChargesRequest
import v1.models.request.RetrievePensionCharges.RetrievePensionChargesRequest
import v1.models.request._

import scala.concurrent.Future

class PensionChargesConnectorSpec extends ConnectorSpec {

  val nino = "AA123456A"

  trait Test {
    _: ConnectorTest =>

    def taxYear: TaxYear

    protected val amendRequest: AmendPensionChargesRequest = AmendPensionChargesRequest(
      nino = Nino(nino),
      taxYear = taxYear,
      pensionCharges = pensionCharges
    )

    protected val deleteRequest: DeletePensionChargesRequest = DeletePensionChargesRequest(
      nino = Nino(nino),
      taxYear = taxYear
    )

    protected val retrieveRequest: RetrievePensionChargesRequest = RetrievePensionChargesRequest(
      nino = Nino(nino),
      taxYear = taxYear
    )

    val connector: PensionChargesConnector =
      new PensionChargesConnector(http = mockHttpClient, appConfig = mockAppConfig)

  }

    "Delete pension charges" when {

      "a valid request is supplied" should {
        "return a successful response with the correct correlationId" in new IfsTest with Test {
          def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

          val expected = Right(ResponseWrapper(correlationId, ()))

          MockedHttpClient
            .delete(
              url = s"$ifsBaseUrl/income-tax/charges/pensions/$nino/$taxYear",
              config = dummyIfsHeaderCarrierConfig,
              requiredHeaders = requiredIfsHeaders,
              excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
            )
            .returns(Future.successful(expected))

          await(connector.deletePensionCharges(deleteRequest)) shouldBe expected
        }
      }

      "a request returning a single error" should {
        "return an unsuccessful response with the correct correlationId and a single error" in new IfsTest with Test {
          def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

          val expected = Left(ResponseWrapper(correlationId, NinoFormatError))

          MockedHttpClient
            .delete(
              url = s"$ifsBaseUrl/income-tax/charges/pensions/$nino/$taxYear",
              config = dummyIfsHeaderCarrierConfig,
              requiredHeaders = requiredIfsHeaders,
              excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
            )
            .returns(Future.successful(expected))

          await(connector.deletePensionCharges(deleteRequest)) shouldBe expected
        }
      }

      "a request returning multiple errors" should {
        "return an unsuccessful response with the correct correlationId and multiple errors" in new IfsTest with Test {

          def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

          val expected = Left(ResponseWrapper(correlationId, Seq(NinoFormatError, StandardDownstreamError)))

          MockedHttpClient
            .delete(
              url = s"$ifsBaseUrl/income-tax/charges/pensions/$nino/$taxYear",
              config = dummyIfsHeaderCarrierConfig,
              requiredHeaders = requiredIfsHeaders,
              excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
            )
            .returns(Future.successful(expected))

          await(connector.deletePensionCharges(deleteRequest)) shouldBe expected
        }
      }
    }

    "Retrieve pension charges" when {

      "a valid request is supplied" should {
        "return a successful response with the correct correlationId" in new DesTest with Test {
          def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

          val expected = Right(ResponseWrapper(correlationId, retrieveResponse))

          MockedHttpClient
            .get(
              url = s"$desBaseUrl/income-tax/charges/pensions/$nino/$taxYear",
              config = dummyHeaderCarrierConfig,
              requiredHeaders = requiredDesHeaders,
              excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
            )
            .returns(Future.successful(expected))

          await(connector.retrievePensionCharges(retrieveRequest)) shouldBe expected
        }
      }

      "a request returning a single error" should {
        "return an unsuccessful response with the correct correlationId and a single error" in new DesTest with Test {
          def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

          val expected = Left(ResponseWrapper(correlationId, NinoFormatError))

          MockedHttpClient
            .get(
              url = s"$desBaseUrl/income-tax/charges/pensions/$nino/$taxYear",
              config = dummyHeaderCarrierConfig,
              requiredHeaders = requiredDesHeaders,
              excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
            )
            .returns(Future.successful(expected))

          await(connector.retrievePensionCharges(retrieveRequest)) shouldBe expected
        }
      }

      "a request returning multiple errors" should {
        "return an unsuccessful response with the correct correlationId and multiple errors" in new DesTest with Test {
          def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

          val expected = Left(ResponseWrapper(correlationId, Seq(NinoFormatError, StandardDownstreamError, TaxYearFormatError)))

          MockedHttpClient
            .get(
              url = s"$desBaseUrl/income-tax/charges/pensions/$nino/$taxYear",
              config = dummyHeaderCarrierConfig,
              requiredHeaders = requiredDesHeaders,
              excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
            )
            .returns(Future.successful(expected))

          await(connector.retrievePensionCharges(retrieveRequest)) shouldBe expected
        }
      }
    }

    "Amend pension charges" when {

      "a valid request is supplied" should {
        "return a successful response with the correct correlationId" in new IfsTest with Test {
          def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

          val expected = Right(ResponseWrapper(correlationId, Unit))

          MockedHttpClient
            .put(
              url = s"$ifsBaseUrl/income-tax/charges/pensions/$nino/$taxYear",
              body = pensionCharges,
              config = dummyIfsHeaderCarrierConfig,
              requiredHeaders = requiredIfsHeaders,
              excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
            )
            .returns(Future.successful(expected))

          await(connector.amendPensionCharges(amendRequest)) shouldBe expected
        }
      }

      "a valid request is supplied for a Tax Year Specific tax year" should {
        "return a successful response with the correct correlationId" in new TysIfsTest with Test {
          def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

          val expected = Right(ResponseWrapper(correlationId, retrieveResponse))

          MockedHttpClient
            .put(
              url = s"$baseUrl/income-tax/charges/pensions/${taxYear.asTysDownstream}/$nino",
              body = pensionCharges,
              config = dummyIfsHeaderCarrierConfig,
              requiredHeaders = requiredTysIfsHeaders,
              excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
            )
            .returns(Future.successful(expected))

          await(connector.amendPensionCharges(amendRequest)) shouldBe expected
        }
      }

      "A request returning a single error" should {
        "return an unsuccessful response with the correct correlationId and a single error" in new IfsTest with Test {
          def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

          val expected = Left(ResponseWrapper(correlationId, NinoFormatError))

          MockedHttpClient
            .put(
              url = s"$ifsBaseUrl/income-tax/charges/pensions/$nino/$taxYear",
              body = pensionCharges,
              config = dummyIfsHeaderCarrierConfig,
              requiredHeaders = requiredIfsHeaders,
              excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
            )
            .returns(Future.successful(expected))

          await(connector.amendPensionCharges(amendRequest)) shouldBe expected
        }
      }

      "a request returning multiple errors" should {
        "return an unsuccessful response with the correct correlationId and multiple errors" in new IfsTest with Test {
          def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

          val expected = Left(ResponseWrapper(correlationId, Seq(NinoFormatError, StandardDownstreamError, TaxYearFormatError)))

          MockedHttpClient
            .put(
              url = s"$ifsBaseUrl/income-tax/charges/pensions/$nino/$taxYear",
              body = pensionCharges,
              config = dummyIfsHeaderCarrierConfig,
              requiredHeaders = requiredIfsHeaders,
              excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
            )
            .returns(Future.successful(expected))

          await(connector.amendPensionCharges(amendRequest)) shouldBe expected
        }
      }
    }

  }