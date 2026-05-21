/*
 * Copyright 2026 HM Revenue & Customs
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

package v3.winterFuelPayment.createAmend

import api.connectors.ConnectorSpec
import api.models.domain.{Nino, TaxYear}
import api.models.errors.{InternalError, MtdError, NinoFormatError, TaxYearFormatError}
import api.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v3.winterFuelPayment.createAmend.models.request.CreateAmendWinterFuelPaymentRequestData
import v3.winterFuelPayment.createAmend.fixture.CreateAmendWinterFuelPaymentFixtures.*

import scala.concurrent.Future

class CreateAmendWinterFuelPaymentConnectorSpec extends ConnectorSpec {

  val nino = "AA123456A"

  trait Test {
    self: ConnectorTest =>

    def taxYear: TaxYear

    protected val request: CreateAmendWinterFuelPaymentRequestData = CreateAmendWinterFuelPaymentRequestData(
      nino = Nino(nino),
      taxYear = taxYear,
      body = requestBodyModel
    )

    val connector: CreateAmendWinterFuelPaymentConnector =
      new CreateAmendWinterFuelPaymentConnector(http = mockHttpClient, appConfig = mockAppConfig)

  }

  "winter fuel payment" when {

    "a valid request is supplied" should {
      "return a successful response with the correct correlationId" in new HipTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2026-27")

        val expected: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

        willPut(url"$baseUrl/itsd/charges/winter-fuel-payment/$nino?taxYear=${taxYear.asTysDownstream}", requestBodyModel)
          .returns(Future.successful(expected))

        await(connector.createAmend(request)) shouldBe expected
      }
    }

    "A request returning a single error" should {
      "return an unsuccessful response with the correct correlationId and a single error" in new HipTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2026-27")

        val expected: Left[ResponseWrapper[NinoFormatError.type], Nothing] = Left(ResponseWrapper(correlationId, NinoFormatError))

        willPut(url"$baseUrl/itsd/charges/winter-fuel-payment/$nino?taxYear=${taxYear.asTysDownstream}", requestBodyModel)
          .returns(Future.successful(expected))

        await(connector.createAmend(request)) shouldBe expected
      }
    }

    "a request returning multiple errors" should {
      "return an unsuccessful response with the correct correlationId and multiple errors" in new HipTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2026-27")

        val expected: Left[ResponseWrapper[Seq[MtdError]], Nothing] =
          Left(ResponseWrapper(correlationId, Seq(NinoFormatError, InternalError, TaxYearFormatError)))

        willPut(url"$baseUrl/itsd/charges/winter-fuel-payment/$nino?taxYear=${taxYear.asTysDownstream}", requestBodyModel)
          .returns(Future.successful(expected))

        await(connector.createAmend(request)) shouldBe expected
      }
    }
  }

}
