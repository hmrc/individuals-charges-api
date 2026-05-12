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

package v3.winterFuelPayment.delete

import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors.{DownstreamErrorCode, DownstreamErrors}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v3.winterFuelPayment.delete.model.request.DeleteWinterFuelPaymentRequestData

import scala.concurrent.Future

class DeleteWinterFuelPaymentConnectorSpec extends ConnectorSpec {

  private val nino    = "AA123456A"
  private val taxYear = TaxYear.fromMtd("2026-27")

  trait Test {
    self: ConnectorTest =>

    protected val connector: DeleteWinterFuelPaymentConnector =
      new DeleteWinterFuelPaymentConnector(http = mockHttpClient, appConfig = mockSharedAppConfig)

    protected val request: DeleteWinterFuelPaymentRequestData = DeleteWinterFuelPaymentRequestData(
      nino = Nino(nino),
      taxYear = taxYear
    )

  }

  "DeleteWinterFuelPaymentConnector" should {
    "return a 204 result on delete" when {
      "the downstream call is successful" in new HipTest with Test {
        val expected: DownstreamOutcome[Unit] = Right(ResponseWrapper(correlationId, ()))

        willDelete(url"$baseUrl/itsd/charges/winter-fuel-payment/$nino?taxYear=${taxYear.asTysDownstream}")
          .returns(Future.successful(expected))

        await(connector.delete(request)) shouldBe expected
      }
    }

    "return an error" when {
      "downstream returns an error" in new HipTest with Test {
        val downstreamErrorResponse: DownstreamErrors = DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))
        val errorOutcome: DownstreamOutcome[Unit] =
          Left(ResponseWrapper(correlationId, downstreamErrorResponse))

        willDelete(url"$baseUrl/itsd/charges/winter-fuel-payment/$nino?taxYear=${taxYear.asTysDownstream}")
          .returns(Future.successful(errorOutcome))

        await(connector.delete(request)) shouldBe errorOutcome
      }
    }
  }

}
