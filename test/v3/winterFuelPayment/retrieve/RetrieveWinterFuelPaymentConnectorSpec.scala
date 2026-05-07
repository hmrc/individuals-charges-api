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

package v3.winterFuelPayment.retrieve

import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.{MtdSourceEnum, Nino, TaxYear}
import shared.models.errors.{DownstreamErrorCode, DownstreamErrors}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v3.winterFuelPayment.retrieve.RetrieveWinterFuelPaymentFixtures.responseModel
import v3.winterFuelPayment.retrieve.model.request.RetrieveWinterFuelPaymentRequestData
import v3.winterFuelPayment.retrieve.model.response.RetrieveWinterFuelPaymentResponse

import scala.concurrent.Future

class RetrieveWinterFuelPaymentConnectorSpec extends ConnectorSpec {
  private val nino: Nino       = Nino("AA123456A")
  private val taxYear: TaxYear = TaxYear.fromMtd("2026-27")
  private val maybeSource: MtdSourceEnum = MtdSourceEnum.`hmrc-held`

  "RetrieveWinterFuelPaymentConnector" should {
    "return a valid response" when {
      "a valid request is made" in new HipTest with Test {
        val outcome: Right[Nothing, ResponseWrapper[RetrieveWinterFuelPaymentResponse]] =
          Right(ResponseWrapper(correlationId, responseModel))

        willGet(url = url"$baseUrl/itsd/charges/winter-fuel-payment/$nino?view=HMRC-HELD")
          .returns(Future.successful(outcome))

        val result: DownstreamOutcome[RetrieveWinterFuelPaymentResponse] =
          await(connector.retrieve(request))

        result shouldBe outcome
      }
    }

    "return an error" when {
      "downstream returns an error" in new HipTest with Test {
        val downstreamErrorResponse: DownstreamErrors = DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))
        val errorOutcome: Left[ResponseWrapper[DownstreamErrors], Nothing] =
          Left(ResponseWrapper(correlationId, downstreamErrorResponse))

        willGet(
          url = url"$baseUrl/itsd/charges/winter-fuel-payment/$nino?view=HMRC-HELD"
        ).returns(Future.successful(errorOutcome))

        val result: DownstreamOutcome[RetrieveWinterFuelPaymentResponse] =
          await(connector.retrieve(request))

        result shouldBe errorOutcome
      }
    }
  }

  trait Test { self: ConnectorTest =>

    protected val connector: RetrieveWinterFuelPaymentConnector =
      new RetrieveWinterFuelPaymentConnector(http = mockHttpClient, appConfig = mockSharedAppConfig)

    protected val request: RetrieveWinterFuelPaymentRequestData = RetrieveWinterFuelPaymentRequestData(
      nino = nino,
      taxYear = taxYear,
      source = maybeSource
    )

  }

}
