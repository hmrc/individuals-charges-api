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

package v1.connectors

import data.AmendPensionChargesData.pensionCharges
import data.RetrievePensionChargesData.retrieveResponse
import mocks.MockAppConfig
import uk.gov.hmrc.domain.Nino
import v1.mocks.MockHttpClient
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.AmendPensionCharges.AmendPensionChargesRequest
import v1.models.request.DeletePensionCharges.DeletePensionChargesRequest
import v1.models.request.RetrievePensionCharges.RetrievePensionChargesRequest
import v1.models.request.{AmendPensionCharges, DeletePensionCharges, RetrievePensionCharges, _}

import scala.concurrent.Future

class PensionChargesConnectorSpec extends ConnectorSpec {

  val nino = "AA123456A"
  val taxYear = "2019-20"

  class Test extends MockHttpClient with MockAppConfig {
    val connector: PensionChargesConnector = new PensionChargesConnector(http = mockHttpClient, appConfig = mockAppConfig)

    val desRequestHeaders: Seq[(String, String)] = Seq("Environment" -> "des-environment", "Authorization" -> s"Bearer des-token")
    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
  }

  "Delete pension charges" when {

    "a valid request is supplied" should {
      "return a successful response with the correct correlationId" in new Test {
        val expected = Right(ResponseWrapper(correlationId, ()))

        MockedHttpClient
          .delete(s"$baseUrl/income-tax/charges/pensions/$nino/$taxYear", desRequestHeaders: _*)
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
          .delete(s"$baseUrl/income-tax/charges/pensions/$nino/$taxYear", desRequestHeaders: _*)
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
          .delete(s"$baseUrl/income-tax/charges/pensions/$nino/$taxYear", desRequestHeaders: _*)
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
          .get(s"$baseUrl/income-tax/charges/pensions/$nino/$taxYear", desRequestHeaders: _*)
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
          .get(s"$baseUrl/income-tax/charges/pensions/$nino/$taxYear", desRequestHeaders: _*)
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
          .get(s"$baseUrl/income-tax/charges/pensions/$nino/$taxYear", desRequestHeaders: _*)
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
          .put(s"$baseUrl/income-tax/charges/pensions/$nino/$taxYear", pensionCharges, desRequestHeaders: _*)
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
          .put(s"$baseUrl/income-tax/charges/pensions/$nino/$taxYear", pensionCharges, desRequestHeaders: _*)
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
          .put(s"$baseUrl/income-tax/charges/pensions/$nino/$taxYear", pensionCharges, desRequestHeaders: _*)
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