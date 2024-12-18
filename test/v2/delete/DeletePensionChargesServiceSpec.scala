/*
 * Copyright 2023 HM Revenue & Customs
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

package v2.delete

import common.controllers.EndpointLogContext
import common.models.domain.{Nino, TaxYear}
import common.errors._
import common.models.outcomes.ResponseWrapper
import common.services.ServiceSpec
import uk.gov.hmrc.http.HeaderCarrier
import v2.delete.def1.request.Def1_DeletePensionChargesRequestData
import v2.delete.model.request.DeletePensionChargesRequestData

import scala.concurrent.Future

class DeletePensionChargesServiceSpec extends ServiceSpec {

  val nino: Nino       = Nino("AA123456A")
  val taxYear: TaxYear = TaxYear.fromMtd("2020-21")

  trait Test extends MockDeletePensionChargesConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    lazy val service = new DeletePensionChargesService(mockDeletePensionChargesConnector)
  }

  lazy val request: DeletePensionChargesRequestData = Def1_DeletePensionChargesRequestData(nino, taxYear)

  "Delete Pension Charges" should {
    "return a Right" when {
      "the connector call is successful" in new Test {
        MockDeletePensionChargesConnector
          .deletePensionCharges(request)
          .returns(Future.successful(Right(ResponseWrapper("resultId", ()))))

        await(service.deletePensionCharges(request)) shouldBe Right(ResponseWrapper("resultId", ()))
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

    "service" when {
      "a service call is successful" should {
        "return a mapped result" in new Test {
          MockDeletePensionChargesConnector
            .deletePensionCharges(request)
            .returns(Future.successful(Right(ResponseWrapper("resultId", ()))))

          await(service.deletePensionCharges(request)) shouldBe Right(ResponseWrapper("resultId", ()))
        }
      }
      "a service call is unsuccessful" should {
        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"return ${error.code} error when $downstreamErrorCode error is returned from the connector" in new Test {

            MockDeletePensionChargesConnector
              .deletePensionCharges(request)
              .returns(Future.successful(Left(ResponseWrapper("resultId", DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            await(service.deletePensionCharges(request)) shouldBe Left(ErrorWrapper("resultId", error))
          }

        val errors = Seq(
          "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
          "INVALID_TAX_YEAR"          -> TaxYearFormatError,
          "NO_DATA_FOUND"             -> NotFoundError,
          "INVALID_CORRELATIONID"     -> InternalError,
          "SERVER_ERROR"              -> InternalError,
          "SERVICE_UNAVAILABLE"       -> InternalError,
          "UNEXPECTED_ERROR"          -> InternalError
        )

        val extraTysErrors = Map(
          "INVALID_CORRELATION_ID" -> InternalError,
          "NOT_FOUND"              -> NotFoundError,
          "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError
        )

        (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
      }
    }
  }

}
