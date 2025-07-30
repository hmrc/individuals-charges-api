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

package v3.highIncomeChildBenefitCharge.createAmend

import common.errors.RuleOutsideAmendmentWindowError
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.{ServiceOutcome, ServiceSpec}
import v3.highIncomeChildBenefitCharge.createAmend.fixture.CreateAmendHighIncomeChildBenefitChargeFixtures.minimumRequestBodyModel
import v3.highIncomeChildBenefitCharge.createAmend.models.request.CreateAmendHighIncomeChildBenefitChargeRequest

import scala.concurrent.Future

class CreateAmendHighIncomeChildBenefitChargeServiceSpec extends ServiceSpec {

  private val nino: Nino       = Nino("AA123456A")
  private val taxYear: TaxYear = TaxYear.fromMtd("2025-26")

  "CreateAmendHighIncomeChildBenefitChargeService" when {
    "createAmend" should {
      "return correct result for a success" in new Test {
        val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

        MockCreateAmendHighIncomeChildBenefitChargeConnector
          .createAmend(request)
          .returns(Future.successful(outcome))

        val result: ServiceOutcome[Unit] = await(service.createAmend(request))

        result shouldBe outcome
      }

      "map errors according to spec" when {
        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {

            MockCreateAmendHighIncomeChildBenefitChargeConnector
              .createAmend(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            val result: ServiceOutcome[Unit] = await(service.createAmend(request))

            result shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errors = List(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("INVALID_CORRELATIONID", InternalError),
          ("TAX_YEAR_NOT_SUPPORTED", RuleTaxYearNotSupportedError),
          ("OUTSIDE_AMENDMENT_WINDOW", RuleOutsideAmendmentWindowError),
          ("INVALID_RANGE_OF_NO_OF_CHILDREN", InternalError),
          ("INVALID_DATE_CEASED", InternalError),
          ("SERVER_ERROR", InternalError),
          ("SERVICE_UNAVAILABLE", InternalError)
        )

        errors.foreach(serviceError.tupled)
      }
    }
  }

  private trait Test extends MockCreateAmendHighIncomeChildBenefitChargeConnector {

    val request: CreateAmendHighIncomeChildBenefitChargeRequest = CreateAmendHighIncomeChildBenefitChargeRequest(
      nino = nino,
      taxYear = taxYear,
      body = minimumRequestBodyModel
    )

    val service: CreateAmendHighIncomeChildBenefitChargeService = new CreateAmendHighIncomeChildBenefitChargeService(
      connector = mockCreateAmendHighIncomeChildBenefitChargeConnector
    )

  }

}
