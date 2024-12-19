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

package v2.createAmend

import shared.connectors.ConnectorSpec
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors.{NinoFormatError, TaxYearFormatError, InternalError}
import shared.models.outcomes.ResponseWrapper
import v2.createAmend.def1.fixture.Def1_CreateAmendPensionChargesFixture.createAmendPensionChargesRequestBody
import v2.createAmend.def1.model.request.Def1_CreateAmendPensionChargesRequestData
import v2.createAmend.model.request.CreateAmendPensionChargesRequestData

import scala.concurrent.Future

class CreateAmendPensionChargesConnectorSpec extends ConnectorSpec {

  val nino = "AA123456A"

  trait Test {
    _: ConnectorTest =>

    def taxYear: TaxYear

    protected val request: CreateAmendPensionChargesRequestData = Def1_CreateAmendPensionChargesRequestData(
      nino = Nino(nino),
      taxYear = taxYear,
      body = createAmendPensionChargesRequestBody
    )

    val connector: CreateAmendPensionChargesConnector =
      new CreateAmendPensionChargesConnector(http = mockHttpClient, appConfig = mockSharedAppConfig)

  }

  "create & amend pension charges" when {

    "a valid request is supplied" should {
      "return a successful response with the correct correlationId" in new IfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

        val expected = Right(ResponseWrapper(correlationId, ()))

        MockedHttpClient
          .put(
            url = s"$baseUrl/income-tax/charges/pensions/$nino/${taxYear.asMtd}",
            body = createAmendPensionChargesRequestBody,
            config = dummyHeaderCarrierConfig,
            requiredHeaders = requiredHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(expected))

        await(connector.createAmendPensionCharges(request)) shouldBe expected
      }
    }

    "a valid request is supplied for a Tax Year Specific tax year" should {
      "return a successful response with the correct correlationId" in new IfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

        val expected = Right(ResponseWrapper(correlationId, ()))

        willPut(s"$baseUrl/income-tax/charges/pensions/23-24/$nino", createAmendPensionChargesRequestBody)
          .returns(Future.successful(expected))

        await(connector.createAmendPensionCharges(request)) shouldBe expected
      }
    }

    "A request returning a single error" should {
      "return an unsuccessful response with the correct correlationId and a single error" in new IfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

        val expected = Left(ResponseWrapper(correlationId, NinoFormatError))

        willPut(s"$baseUrl/income-tax/charges/pensions/$nino/${taxYear.asMtd}", createAmendPensionChargesRequestBody)
          .returns(Future.successful(expected))

        await(connector.createAmendPensionCharges(request)) shouldBe expected
      }
    }

    "a request returning multiple errors" should {
      "return an unsuccessful response with the correct correlationId and multiple errors" in new IfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

        val expected = Left(ResponseWrapper(correlationId, Seq(NinoFormatError, InternalError, TaxYearFormatError)))

        willPut(s"$baseUrl/income-tax/charges/pensions/$nino/${taxYear.asMtd}", createAmendPensionChargesRequestBody)
          .returns(Future.successful(expected))

        await(connector.createAmendPensionCharges(request)) shouldBe expected
      }
    }
  }

}
