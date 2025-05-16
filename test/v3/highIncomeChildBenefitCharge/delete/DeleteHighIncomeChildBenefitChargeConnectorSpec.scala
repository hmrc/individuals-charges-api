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

package v3.highIncomeChildBenefitCharge.delete

import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors.{DownstreamErrorCode, DownstreamErrors}
import shared.models.outcomes.ResponseWrapper
import v3.highIncomeChildBenefitCharge.delete.model.request.DeleteHighIncomeChildBenefitChargeRequestData

import scala.concurrent.Future

class DeleteHighIncomeChildBenefitChargeConnectorSpec extends ConnectorSpec {

  private val nino: Nino                 = Nino("AA123456A")
  private val taxYear: TaxYear           = TaxYear.fromMtd("2025-26")

  "DeleteHighIncomeChildBenefitConnector" should {
    "return a 204 result on delete for a TYS request" when {
      "the downstream call is successful and tax year specific" in new HipTest with Test {
        val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

        willDelete(s"$baseUrl/itsa/income-tax/v1/${taxYear.asTysDownstream}/high-income-child-benefit/charges/$nino")
          .returns(Future.successful(outcome))


        val result: DownstreamOutcome[Unit] = await(connector.deleteHighIncomeChildBenefit(request))

        result shouldBe outcome
      }
    }

    "return an error" when {
      "downstream returns an error" in new HipTest with Test {
        val downstreamErrorResponse: DownstreamErrors = DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))
        val errorOutcome: Left[ResponseWrapper[DownstreamErrors], Nothing] =
          Left(ResponseWrapper(correlationId, downstreamErrorResponse))

        willDelete(s"$baseUrl/itsa/income-tax/v1/${taxYear.asTysDownstream}/high-income-child-benefit/charges/$nino")
          .returns(Future.successful(errorOutcome))

        val result: DownstreamOutcome[Unit] = await(connector.deleteHighIncomeChildBenefit(request))

        result shouldBe errorOutcome
      }
    }
  }

  trait Test { _: ConnectorTest =>

    protected val connector: DeleteHighIncomeChildBenefitChargeConnector =
      new DeleteHighIncomeChildBenefitChargeConnector(http = mockHttpClient, appConfig = mockSharedAppConfig)

    protected val request: DeleteHighIncomeChildBenefitChargeRequestData = DeleteHighIncomeChildBenefitChargeRequestData(
      nino = nino,
      taxYear = taxYear
    )
  }

}
