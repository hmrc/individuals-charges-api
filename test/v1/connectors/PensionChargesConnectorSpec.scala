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

import data.AmendPensionChargesData
import data.AmendPensionChargesData.{fullJson, pensionCharges}
import mocks.MockAppConfig
import play.api.mvc.AnyContentAsJson
import uk.gov.hmrc.domain.Nino
import v1.mocks.MockHttpClient
import v1.models.des._
import v1.models.errors._
import v1.models.outcomes.DesResponse
import v1.models.requestData._

import scala.concurrent.Future

class PensionChargesConnectorSpec extends ConnectorSpec {

  lazy val baseUrl = "test-BaseUrl"
  val correlationId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  val nino = "AA123456A"
  val taxYear = "2019-20"

  val pensionSavingsCharge: PensionSavingsTaxCharges = PensionSavingsTaxCharges(
    Seq("00123456RA", "00123456RA"),
    Some(LifetimeAllowance(100.00, 100.00)),
    Some(LifetimeAllowance(100.00, 100.00)),
    true,
    Some(true),
    Some(true),
  )

  val overseasSchemeProvider: OverseasSchemeProvider = OverseasSchemeProvider(
    "name",
    "address",
    "postcode",
    Some(Seq("Q123456")),
    None
  )

  val pensionOverseasTransfer: PensionSchemeOverseasTransfers = PensionSchemeOverseasTransfers(
    Seq(overseasSchemeProvider),
    100.00,
    100.00
  )

  val pensionUnauthorisedPayments: PensionSchemeUnauthorisedPayments = PensionSchemeUnauthorisedPayments(
    Seq("00123456RA", "00123456RA"),
    Some(Charge(100.00, 100.00)),
    Some(Charge(100.00, 100.00))
  )

  val pensionContributions: PensionContributions = PensionContributions(
    Seq("00123456RA", "00123456RA"),
    100.00,
    100.00
  )

  val overseasPensionContributions: OverseasPensionContributions = OverseasPensionContributions(
    Seq(overseasSchemeProvider),
    100.00,
    100.00
  )

  val retrieveResponse: RetrievePensionChargesResponse = RetrievePensionChargesResponse(
    Some(pensionSavingsCharge),
    Some(pensionOverseasTransfer),
    Some(pensionUnauthorisedPayments),
    Some(pensionContributions),
    Some(overseasPensionContributions)
  )

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
        val expected = Right(DesResponse(correlationId, ()))

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
        val expected = Left(DesResponse(correlationId, SingleError(NinoFormatError)))

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

    "a request returning multiple errors" should {
      "return an unsuccessful response with the correct correlationId and multiple errors" in new Test {
        val expected = Left(DesResponse(correlationId, MultipleErrors(Seq(NinoFormatError, DownstreamError))))

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
  }

  "Retrieve pension charges" when {
    "a valid request is supplied" should {
      "return a successful response with the correct correlationId" in new Test {
        val expected = Right(DesResponse(correlationId, retrieveResponse))

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
        val expected = Left(DesResponse(correlationId, SingleError(NinoFormatError)))

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

    "a request returning multiple errors" should {
      "return an unsuccessful response with the correct correlationId and multiple errors" in new Test {
        val expected = Left(DesResponse(correlationId, MultipleErrors(Seq(NinoFormatError, DownstreamError, TaxYearFormatError))))

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
  }
  "Amend pension charges" when {
    "a valid request is supplied" should {
      "return a successful response with the correct correlationId" in new Test {
        val expected = Left(DesResponse(correlationId, Unit))

        MockedHttpClient
          .put(s"$baseUrl/income-tax/charges/pensions/$nino/$taxYear", PensionCharges, desRequestHeaders: _*)
          .returns(Future.successful(expected))

        await(connector.amendPensionCharges(
          AmendPensionChargesRequest(
            nino = Nino(nino),
            taxYear = DesTaxYear(taxYear),
            pensionCharges
          )
        ))
      }
    }
  }
}
