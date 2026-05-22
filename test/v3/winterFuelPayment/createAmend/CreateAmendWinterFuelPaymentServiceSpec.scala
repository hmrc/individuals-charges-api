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

package v3.winterFuelPayment.createAmend

import api.models.domain.{Nino, TaxYear}
import api.models.errors.*
import api.models.outcomes.ResponseWrapper
import api.services.{ServiceOutcome, ServiceSpec}
import common.errors.RuleOutsideAmendmentWindowError
import v3.winterFuelPayment.createAmend.fixture.CreateAmendWinterFuelPaymentFixtures.*
import v3.winterFuelPayment.createAmend.models.request.CreateAmendWinterFuelPaymentRequestData

import scala.concurrent.Future

class CreateAmendWinterFuelPaymentServiceSpec extends ServiceSpec {

  private val nino: Nino       = Nino("AA123456A")
  private val taxYear: TaxYear = TaxYear.fromMtd("2026-27")

  "CreateAmendWinterFuelPaymentChargeService" when {
    "createAmend" should {
      "return correct result for a success" in new Test {
        val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

        MockCreateAmendWinterFuelPaymentConnector
          .createAmend(request)
          .returns(Future.successful(outcome))

        val result: ServiceOutcome[Unit] = await(service.createAmend(request))

        result shouldBe outcome
      }

      "map errors according to spec" when {
        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {

            MockCreateAmendWinterFuelPaymentConnector
              .createAmend(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            val result: ServiceOutcome[Unit] = await(service.createAmend(request))

            result shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errors = List(
          ("1000", InternalError),
          ("1117", TaxYearFormatError),
          ("1215", NinoFormatError),
          ("1216", InternalError),
          ("UNMATCHED_STUB_ERROR", RuleIncorrectGovTestScenarioError),
          ("1115", RuleTaxYearNotEndedError),
          ("1263", RuleWFPAmountAboveMaximumError),
          ("1264", RuleWFPAmountBelowMinimumError),
          ("4200", RuleOutsideAmendmentWindowError),
          ("5000", InternalError)
        )

        errors.foreach(serviceError.tupled)
      }
    }
  }

  private trait Test extends MockCreateAmendWinterFuelPaymentConnector {

    val request: CreateAmendWinterFuelPaymentRequestData = CreateAmendWinterFuelPaymentRequestData(
      nino = nino,
      taxYear = taxYear,
      body = requestBodyModel
    )

    val service: CreateAmendWinterFuelPaymentService = new CreateAmendWinterFuelPaymentService(
      connector = mockCreateAmendWinterFuelPaymentConnector
    )

  }

}
