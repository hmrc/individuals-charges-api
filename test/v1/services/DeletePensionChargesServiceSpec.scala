/*
 * Copyright 2021 HM Revenue & Customs
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

package v1.services

import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.controllers.EndpointLogContext
import v1.mocks.connectors.MockPensionChargesConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.DeletePensionCharges.DeletePensionChargesRequest
import v1.models.request.DesTaxYear

import scala.concurrent.Future

class DeletePensionChargesServiceSpec extends ServiceSpec {

  val nino = Nino("AA123456A")
  val taxYear = DesTaxYear("2020-21")

  trait Test extends MockPensionChargesConnector {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    lazy val service = new DeletePensionChargesService(connector)
  }

  lazy val request = DeletePensionChargesRequest(nino, taxYear)

  "Delete Pension Charges" should {
    "return a Right" when {
      "the connector call is successful" in new Test {
        MockPensionChargesConnector.deletePensionCharges(request).returns(Future.successful(Right(ResponseWrapper("resultId", ()))))

        await(service.deletePensionCharges(request)) shouldBe Right(ResponseWrapper("resultId", ()))
      }
    }

    "return that wrapped error as-is" when {
      "the connector returns an outbound error" in new Test {
        val someError:MtdError = DownstreamError
        val desResponse = ResponseWrapper(correlationId, OutboundError(someError))
        MockPensionChargesConnector.deletePensionCharges(request).returns(Future.successful(Left(desResponse)))

        await(service.deletePensionCharges(request)) shouldBe Left(ErrorWrapper(correlationId, someError))
      }
    }

    "service" when {
      "a service call is successful" should {
        "return a mapped result" in new Test {
          MockPensionChargesConnector.deletePensionCharges(request)
            .returns(Future.successful(Right(ResponseWrapper("resultId", ()))))

          await(service.deletePensionCharges(request)) shouldBe Right(ResponseWrapper("resultId", ()))
        }
      }
      "a service call is unsuccessful" should {
        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"return ${error.code} error when $desErrorCode error is returned from the connector" in new Test {

            MockPensionChargesConnector.deletePensionCharges(request)
              .returns(Future.successful(Left(ResponseWrapper("resultId", DesErrors.single(DesErrorCode(desErrorCode))))))

            await(service.deletePensionCharges(request)) shouldBe Left(ErrorWrapper("resultId", error))
          }

        val input = Seq(
          "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
          "INVALID_TAX_YEAR"          -> TaxYearFormatError,
          "NOT_FOUND"                 -> NotFoundError,
          "INVALID_CORRELATIONID"     -> DownstreamError,
          "SERVER_ERROR"              -> DownstreamError,
          "SERVICE_UNAVAILABLE"       -> DownstreamError,
          "UNEXPECTED_ERROR"    -> DownstreamError
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }
}
