/*
 * Copyright 2024 HM Revenue & Customs
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

package v3.pensionCharges.delete

import common.errors.RuleOutsideAmendmentWindowError
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors.*
import shared.models.outcomes.ResponseWrapper
import shared.services.ServiceSpec
import v3.pensionCharges.delete.def1.request.Def1_DeletePensionChargesRequestData
import v3.pensionCharges.delete.model.request.DeletePensionChargesRequestData

import scala.concurrent.Future

class DeletePensionChargesServiceSpec extends ServiceSpec {

  val nino: Nino       = Nino("AA123456A")
  val taxYear: TaxYear = TaxYear.fromMtd("2020-21")

  trait Test extends MockDeletePensionChargesConnector {
    lazy val service = new DeletePensionChargesService(mockDeletePensionChargesConnector)
  }

  lazy val request: DeletePensionChargesRequestData = Def1_DeletePensionChargesRequestData(nino, taxYear)

  "Delete Pension Charges" should {
    "return a Right" when {
      "the connector call is successful" in new Test {
        MockDeletePensionChargesConnector
          .deletePensionCharges(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        await(service.deletePensionCharges(request)) shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }

    "return that wrapped error as-is" when {
      "the connector returns an outbound error" in new Test {
        val someError: MtdError                                = InternalError
        val downstreamResponse: ResponseWrapper[OutboundError] = ResponseWrapper(correlationId, OutboundError(someError))
        MockDeletePensionChargesConnector.deletePensionCharges(request).returns(Future.successful(Left(downstreamResponse)))

        await(service.deletePensionCharges(request)) shouldBe Left(ErrorWrapper(correlationId, someError))
      }
    }

    "map errors according to spec" when {
      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"a code $downstreamErrorCode error is returned from the service" in new Test {

          MockDeletePensionChargesConnector
            .deletePensionCharges(request)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          await(service.deletePensionCharges(request)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errors = Seq(
        "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
        "INVALID_TAX_YEAR"          -> TaxYearFormatError,
        "NO_DATA_FOUND"             -> NotFoundError,
        "INVALID_CORRELATIONID"     -> InternalError,
        "SERVER_ERROR"              -> InternalError,
        "SERVICE_UNAVAILABLE"       -> InternalError,
        "UNEXPECTED_ERROR"          -> InternalError,
        "OUTSIDE_AMENDMENT_WINDOW"  -> RuleOutsideAmendmentWindowError
      )

      val extraTysErrors = Map(
        "INVALID_CORRELATION_ID" -> InternalError,
        "NOT_FOUND"              -> NotFoundError,
        "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError
      )

      (errors ++ extraTysErrors).foreach(serviceError.tupled)
    }
  }

}
