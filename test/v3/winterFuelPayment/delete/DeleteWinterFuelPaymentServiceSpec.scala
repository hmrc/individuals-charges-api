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

import common.errors.RuleOutsideAmendmentWindowError
import api.models.domain.{Nino, TaxYear}
import api.models.errors.*
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import v3.winterFuelPayment.delete.model.request.DeleteWinterFuelPaymentRequestData

import scala.concurrent.Future

class DeleteWinterFuelPaymentServiceSpec extends ServiceSpec {

  val nino: Nino       = Nino("AA123456A")
  val taxYear: TaxYear = TaxYear.fromMtd("2026-27")

  trait Test extends MockDeleteWinterFuelPaymentConnector {
    lazy val service = new DeleteWinterFuelPaymentService(mockDeleteWinterFuelPaymentConnector)
  }

  lazy val request: DeleteWinterFuelPaymentRequestData = DeleteWinterFuelPaymentRequestData(nino, taxYear)

  "Delete Winter Fuel Payment" should {
    "return a Right" when {
      "the connector call is successful" in new Test {
        MockDeleteWinterFuelPaymentConnector
          .delete(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        await(service.delete(request)) shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }

    "return that wrapped error as-is" when {
      "the connector returns an outbound error" in new Test {
        val someError: MtdError                                = InternalError
        val downstreamResponse: ResponseWrapper[OutboundError] = ResponseWrapper(correlationId, OutboundError(someError))
        MockDeleteWinterFuelPaymentConnector.delete(request).returns(Future.successful(Left(downstreamResponse)))

        await(service.delete(request)) shouldBe Left(ErrorWrapper(correlationId, someError))
      }
    }

    "map errors according to spec" when {
      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"a code $downstreamErrorCode error is returned from the service" in new Test {

          MockDeleteWinterFuelPaymentConnector
            .delete(request)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          await(service.delete(request)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errors = Seq(
        "1117"                 -> TaxYearFormatError,
        "1215"                 -> NinoFormatError,
        "1216"                 -> InternalError,
        "UNMATCHED_STUB_ERROR" -> RuleIncorrectGovTestScenarioError,
        "5010"                 -> NotFoundError,
        "4200"                 -> RuleOutsideAmendmentWindowError,
        "5000"                 -> InternalError
      )

      errors.foreach(serviceError.tupled)

    }
  }

}
