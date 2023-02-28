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
import api.models.errors.{InternalError, NinoFormatError}
import api.models.outcomes.ResponseWrapper
import v1.models.request.DeletePensionCharges.DeletePensionChargesRequest

import scala.concurrent.Future

class DeletePensionChargesConnectorSpec extends ConnectorSpec {

  val nino = "AA123456A"

  trait Test {
    _: ConnectorTest =>

    def taxYear: TaxYear

    protected val request: DeletePensionChargesRequest = DeletePensionChargesRequest(
      nino = Nino(nino),
      taxYear = taxYear
    )

    val connector: DeletePensionChargesConnector =
      new DeletePensionChargesConnector(http = mockHttpClient, appConfig = mockAppConfig)

  }

  "Delete pension charges" should {
    "return the expected response for a non-TYS request" when {
      "a valid request is made" in new IfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

        val expected = Right(ResponseWrapper(correlationId, ()))

        willDelete(
          url = s"$baseUrl/income-tax/charges/pensions/$nino/${taxYear.asMtd}"
        ).returns(Future.successful(expected))

        await(connector.deletePensionCharges(request)) shouldBe expected
      }

      "downstream returns a single error" in new IfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

        val expected = Left(ResponseWrapper(correlationId, NinoFormatError))

        willDelete(
          url = s"$baseUrl/income-tax/charges/pensions/$nino/${taxYear.asMtd}"
        ).returns(Future.successful(expected))

        await(connector.deletePensionCharges(request)) shouldBe expected
      }

      "downstream returns multiple errors" in new IfsTest with Test {

        def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

        val expected = Left(ResponseWrapper(correlationId, Seq(NinoFormatError, InternalError)))

        willDelete(
          url = s"$baseUrl/income-tax/charges/pensions/$nino/${taxYear.asMtd}"
        ).returns(Future.successful(expected))

        await(connector.deletePensionCharges(request)) shouldBe expected
      }

    }
    "return the expected response for a TYS request" when {
      "a valid request is made" in new TysIfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

        val expected = Right(ResponseWrapper(correlationId, ()))

        willDelete(
          url = s"$baseUrl/income-tax/charges/pensions/${taxYear.asTysDownstream}/$nino"
        ).returns(Future.successful(expected))

        await(connector.deletePensionCharges(request)) shouldBe expected
      }
    }
  }

}
