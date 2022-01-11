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

package v1r6.connectors

import mocks.{MockAppConfig, MockHttpClient}
import v1r6.data.AmendPensionChargesData.pensionCharges
import v1r6.data.RetrievePensionChargesData.retrieveResponse
import v1r6.models.domain.Nino
import v1r6.models.errors._
import v1r6.models.outcomes.ResponseWrapper
import v1r6.models.request.AmendPensionCharges.AmendPensionChargesRequest
import v1r6.models.request.DeletePensionCharges.DeletePensionChargesRequest
import v1r6.models.request.RetrievePensionCharges.RetrievePensionChargesRequest
import v1r6.models.request._

import scala.concurrent.Future

class PensionChargesConnectorSpec extends ConnectorSpec {

  val nino = "AA123456A"
  val taxYear = "2019-20"

  class Test extends MockHttpClient with MockAppConfig {
    val connector: PensionChargesConnector = new PensionChargesConnector(http = mockHttpClient, appConfig = mockAppConfig)

    val desRequestHeaders: Seq[(String, String)] = Seq("Environment" -> "des-environment", "Authorization" -> s"Bearer des-token")
    MockAppConfig.desBaseUrl returns baseUrl
    MockAppConfig.desToken returns "des-token"
    MockAppConfig.desEnvironment returns "des-environment"
    MockAppConfig.desEnvironmentHeaders returns Some(allowedDesHeaders)

    val ifsRequestHeaders: Seq[(String, String)] = Seq("Environment" -> "ifs-environment", "Authorization" -> s"Bearer ifs-token")
    MockAppConfig.ifsBaseUrl returns baseUrl
    MockAppConfig.ifsToken returns "ifs-token"
    MockAppConfig.ifsEnvironment returns "ifs-environment"
    MockAppConfig.ifsEnvironmentHeaders returns Some(allowedIfsHeaders)
  }

  "Delete pension charges" when {

    "a valid request is supplied" should {
      "return a successful response with the correct correlationId" in new Test {
        val expected = Right(ResponseWrapper(correlationId, ()))

        MockedHttpClient
          .delete(
            url =s"$baseUrl/income-tax/charges/pensions/$nino/$taxYear",
            config = dummyDesHeaderCarrierConfig,
            requiredHeaders = requiredDesHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue"))
          .returns(Future.successful(expected))

        await(connector.deletePensionCharges(
          DeletePensionChargesRequest(
            nino = Nino(nino),
            taxYear = DesTaxYear(taxYear)
          )
        )) shouldBe expected
      }
    }

    "a request returning a single error" should {
      "return an unsuccessful response with the correct correlationId and a single error" in new Test {
        val expected = Left(ResponseWrapper(correlationId, NinoFormatError))

        MockedHttpClient
          .delete(
            url =s"$baseUrl/income-tax/charges/pensions/$nino/$taxYear",
            config = dummyDesHeaderCarrierConfig,
            requiredHeaders = requiredDesHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue"))
          .returns(Future.successful(expected))

        await(connector.deletePensionCharges(
          DeletePensionCharges.DeletePensionChargesRequest(
            nino = Nino(nino),
            taxYear = DesTaxYear(taxYear)
          )
        )) shouldBe expected
      }
    }

    "a request returning multiple errors" should {
      "return an unsuccessful response with the correct correlationId and multiple errors" in new Test {
        val expected = Left(ResponseWrapper(correlationId, Seq(NinoFormatError, DownstreamError)))

        MockedHttpClient
          .delete(
            url =s"$baseUrl/income-tax/charges/pensions/$nino/$taxYear",
            config = dummyDesHeaderCarrierConfig,
            requiredHeaders = requiredDesHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue"))
          .returns(Future.successful(expected))

        await(connector.deletePensionCharges(
          DeletePensionCharges.DeletePensionChargesRequest(
            nino = Nino(nino),
            taxYear = DesTaxYear(taxYear)
          )
        )) shouldBe expected
      }
    }
  }

  "Retrieve pension charges" when {
    "a valid request is supplied" should {
      "return a successful response with the correct correlationId" in new Test {
        val expected = Right(ResponseWrapper(correlationId, retrieveResponse))

        MockedHttpClient
          .get(
            url =s"$baseUrl/income-tax/charges/pensions/$nino/$taxYear",
            config = dummyDesHeaderCarrierConfig,
            requiredHeaders = requiredDesHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue"))
          .returns(Future.successful(expected))

        await(connector.retrievePensionCharges(
          RetrievePensionChargesRequest(
            nino = Nino(nino),
            taxYear = DesTaxYear(taxYear)
          )
        )) shouldBe expected
      }
    }

    "a request returning a single error" should {
      "return an unsuccessful response with the correct correlationId and a single error" in new Test {
        val expected = Left(ResponseWrapper(correlationId, NinoFormatError))

        MockedHttpClient
          .get(
            url =s"$baseUrl/income-tax/charges/pensions/$nino/$taxYear",
            config = dummyDesHeaderCarrierConfig,
            requiredHeaders = requiredDesHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue"))
          .returns(Future.successful(expected))

        await(connector.retrievePensionCharges(
          RetrievePensionCharges.RetrievePensionChargesRequest(
            nino = Nino(nino),
            taxYear = DesTaxYear(taxYear)
          )
        )) shouldBe expected
      }
    }

    "a request returning multiple errors" should {
      "return an unsuccessful response with the correct correlationId and multiple errors" in new Test {
        val expected = Left(ResponseWrapper(correlationId, Seq(NinoFormatError, DownstreamError, TaxYearFormatError)))

        MockedHttpClient
          .get(
            url =s"$baseUrl/income-tax/charges/pensions/$nino/$taxYear",
            config = dummyDesHeaderCarrierConfig,
            requiredHeaders = requiredDesHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue"))
          .returns(Future.successful(expected))

        await(connector.retrievePensionCharges(
          RetrievePensionCharges.RetrievePensionChargesRequest(
            nino = Nino(nino),
            taxYear = DesTaxYear(taxYear)
          )
        )) shouldBe expected
      }
    }
  }

  "Amend pension charges" when {

    "a valid request is supplied" should {
      "return a successful response with the correct correlationId" in new Test {
        val expected = Right(ResponseWrapper(correlationId, Unit))

        MockedHttpClient
          .put(
            url =s"$baseUrl/income-tax/charges/pensions/$nino/$taxYear",
            body = pensionCharges,
            config = dummyIfsHeaderCarrierConfig,
            requiredHeaders = requiredIfsHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue"))
          .returns(Future.successful(expected))

        await(connector.amendPensionCharges(
          AmendPensionChargesRequest(
            nino = Nino(nino),
            taxYear = DesTaxYear(taxYear),
            pensionCharges
          )
        )) shouldBe expected
      }
    }
    "A request returning a single error" should {
      "return an unsuccessful response with the correct correlationId and a single error" in new Test {
        val expected = Left(ResponseWrapper(correlationId, NinoFormatError))

        MockedHttpClient
          .put(
            url =s"$baseUrl/income-tax/charges/pensions/$nino/$taxYear",
            body = pensionCharges,
            config = dummyIfsHeaderCarrierConfig,
            requiredHeaders = requiredIfsHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue"))
          .returns(Future.successful(expected))

        await(connector.amendPensionCharges(
          AmendPensionCharges.AmendPensionChargesRequest(
            nino = Nino(nino),
            taxYear = DesTaxYear(taxYear),
            pensionCharges
          )
        )) shouldBe expected
      }
    }
    "a request returning multiple errors" should {
      "return an unsuccessful response with the correct correlationId and multiple errors" in new Test {
        val expected = Left(ResponseWrapper(correlationId, Seq(NinoFormatError, DownstreamError, TaxYearFormatError)))

        MockedHttpClient
          .put(
            url =s"$baseUrl/income-tax/charges/pensions/$nino/$taxYear",
            body = pensionCharges,
            config = dummyIfsHeaderCarrierConfig,
            requiredHeaders = requiredIfsHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue"))
          .returns(Future.successful(expected))

        await(connector.amendPensionCharges(
          AmendPensionCharges.AmendPensionChargesRequest(
            nino = Nino(nino),
            taxYear = DesTaxYear(taxYear),
            pensionCharges
          )
        )) shouldBe expected
      }
    }
  }
}