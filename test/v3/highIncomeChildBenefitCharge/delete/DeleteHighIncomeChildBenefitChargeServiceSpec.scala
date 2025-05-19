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

import common.errors.RuleOutsideAmendmentWindowError
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.{ServiceOutcome, ServiceSpec}
import v3.highIncomeChildBenefitCharge.delete.model.request.DeleteHighIncomeChildBenefitChargeRequestData

import scala.concurrent.Future

class DeleteHighIncomeChildBenefitChargeServiceSpec extends ServiceSpec {

  private val nino: Nino                 = Nino("AA123456A")
  private val taxYear: TaxYear           = TaxYear.fromMtd("2025-26")

  "DeleteHighIncomeChildBenefitChargeService" when {
    "delete" should {
      "return correct result for a success" in new Test {
        val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

        MockDeleteHighIncomeChildBenefitChargeConnector
          .delete(request)
          .returns(Future.successful(outcome))

        val result: ServiceOutcome[Unit] = await(service.delete(request))

        result shouldBe outcome
      }

      "map errors according to spec" when {
        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {

            MockDeleteHighIncomeChildBenefitChargeConnector
              .delete(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            val result: ServiceOutcome[Unit] = await(service.delete(request))

            result shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errors = List(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("INVALID_CORRELATIONID", InternalError),
          ("NOT_FOUND", NotFoundError),
          ("TAX_YEAR_NOT_SUPPORTED", RuleTaxYearNotSupportedError),
          ("OUTSIDE_AMENDMENT_WINDOW", RuleOutsideAmendmentWindowError),
          ("SERVER_ERROR", InternalError),
          ("SERVICE_UNAVAILABLE", InternalError)
        )

        errors.foreach(args => (serviceError _).tupled(args))
      }
    }
  }

  trait Test extends MockDeleteHighIncomeChildBenefitChargeConnector {

    val request: DeleteHighIncomeChildBenefitChargeRequestData = DeleteHighIncomeChildBenefitChargeRequestData(
      nino = nino,
      taxYear = taxYear
    )

    val service: DeleteHighIncomeChildBenefitChargeService = new DeleteHighIncomeChildBenefitChargeService(
      connector = mockDeleteHighIncomeChildBenefitChargeConnector
    )

  }

}
