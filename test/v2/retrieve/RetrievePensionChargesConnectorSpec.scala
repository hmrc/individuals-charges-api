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

package v2.retrieve

import shared.connectors.ConnectorSpec
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors.{InternalError, NinoFormatError, TaxYearFormatError}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v2.retrieve.def1.fixture.RetrievePensionChargesFixture.retrieveResponse
import v2.retrieve.def1.model.request.Def1_RetrievePensionChargesRequestData
import v2.retrieve.model.request.RetrievePensionChargesRequestData

import scala.concurrent.Future

class RetrievePensionChargesConnectorSpec extends ConnectorSpec {

  val nino = "AA123456A"

  trait Test {
    _: ConnectorTest =>

    def taxYear: TaxYear

    protected val request: RetrievePensionChargesRequestData = Def1_RetrievePensionChargesRequestData(
      nino = Nino(nino),
      taxYear = taxYear
    )

    val connector: RetrievePensionChargesConnector =
      new RetrievePensionChargesConnector(http = mockHttpClient, appConfig = mockSharedAppConfig)

  }

  "Retrieve pension charges" when {

    "a valid request is supplied" should {
      "return a successful response with the correct correlationId" in new IfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

        val expected = Right(ResponseWrapper(correlationId, retrieveResponse))

        willGet(url"$baseUrl/income-tax/charges/pensions/$nino/${taxYear.asMtd}")
          .returns(Future.successful(expected))

        await(connector.retrievePensionCharges(request)) shouldBe expected
      }
    }

    "a valid request is supplied for a Tax Year Specific tax year" should {
      "return a successful response with the correct correlationId" in new IfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

        val expected = Right(ResponseWrapper(correlationId, retrieveResponse))

        willGet(url"$baseUrl/income-tax/charges/pensions/23-24/$nino")
          .returns(Future.successful(expected))

        await(connector.retrievePensionCharges(request)) shouldBe expected
      }
    }

    "a request returning a single error" should {
      "return an unsuccessful response with the correct correlationId and a single error" in new IfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

        val expected = Left(ResponseWrapper(correlationId, NinoFormatError))

        willGet(url"$baseUrl/income-tax/charges/pensions/$nino/${taxYear.asMtd}")
          .returns(Future.successful(expected))

        await(connector.retrievePensionCharges(request)) shouldBe expected
      }
    }

    "a request returning multiple errors" should {
      "return an unsuccessful response with the correct correlationId and multiple errors" in new IfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

        val expected = Left(ResponseWrapper(correlationId, Seq(NinoFormatError, InternalError, TaxYearFormatError)))

        willGet(url"$baseUrl/income-tax/charges/pensions/$nino/${taxYear.asMtd}")
          .returns(Future.successful(expected))

        await(connector.retrievePensionCharges(request)) shouldBe expected
      }
    }
  }

}
