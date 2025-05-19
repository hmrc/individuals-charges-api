/*
 * Copyright 2025 HM Revenue & Customs
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

package v3.highIncomeChildBenefitCharge.retrieve

import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors.{DownstreamErrorCode, DownstreamErrors}
import shared.models.outcomes.ResponseWrapper
import v3.highIncomeChildBenefitCharge.retrieve.RetrieveHighIncomeChildBenefitFixtures.responseModel
import v3.highIncomeChildBenefitCharge.retrieve.model.{RetrieveHighIncomeChildBenefitChargeRequest, RetrieveHighIncomeChildBenefitChargeResponse}

import scala.concurrent.Future

class RetrieveHighIncomeChildBenefitChargeConnectorSpec extends ConnectorSpec {
  private val nino: Nino                 = Nino("AA123456A")
  private val taxYear: TaxYear           = TaxYear.fromMtd("2025-26")

  "RetrieveHighIncomeChildBenefitChargeConnector" should {
    "return a valid response" when {
      "a valid request is made" in new HipTest with Test {
        val outcome: Right[Nothing, ResponseWrapper[RetrieveHighIncomeChildBenefitChargeResponse]] =
          Right(ResponseWrapper(correlationId, responseModel))

        willGet(url = s"$baseUrl/itsa/income-tax/v1/${taxYear.asTysDownstream}/high-income-child-benefit/charges/$nino")
          .returns(Future.successful(outcome))

        val result: DownstreamOutcome[RetrieveHighIncomeChildBenefitChargeResponse] =
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
          url = s"$baseUrl/itsa/income-tax/v1/${taxYear.asTysDownstream}/high-income-child-benefit/charges/$nino"
        ).returns(Future.successful(errorOutcome))

        val result: DownstreamOutcome[RetrieveHighIncomeChildBenefitChargeResponse] =
          await(connector.retrieve(request))

        result shouldBe errorOutcome
      }
    }
  }

  trait Test { _: ConnectorTest =>

    protected val connector: RetrieveHighIncomeChildBenefitChargeConnector =
      new RetrieveHighIncomeChildBenefitChargeConnector(http = mockHttpClient, appConfig = mockSharedAppConfig)

    protected val request: RetrieveHighIncomeChildBenefitChargeRequest = RetrieveHighIncomeChildBenefitChargeRequest(
      nino = nino,
      taxYear = taxYear
    )
  }

}
