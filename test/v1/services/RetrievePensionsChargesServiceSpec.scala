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

import v1.models.request.DesTaxYear
import data.RetrievePensionChargesData._
import uk.gov.hmrc.http.HeaderCarrier
import v1.controllers.EndpointLogContext
import v1.models.outcomes.ResponseWrapper
import v1.mocks.connectors.MockPensionChargesConnector
import v1.models.domain.Nino
import v1.models.errors._
import v1.models.request.RetrievePensionCharges.RetrievePensionChargesRequest

import scala.concurrent.Future

class RetrievePensionsChargesServiceSpec extends ServiceSpec {

  val nino: Nino = Nino("AA123456A")
  val taxYear: DesTaxYear = DesTaxYear("2020-21")

  private val request = RetrievePensionChargesRequest(nino, taxYear)

  trait Test extends MockPensionChargesConnector {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")
    val service = new RetrievePensionChargesService(connector)
  }

  "Retrieve Pension Charges" should {
    "return a valid response" when {
      "a valid request is supplied" in new Test {
        MockPensionChargesConnector.retrievePensions(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, retrieveResponse))))

        await(service.retrievePensions(request)) shouldBe Right(ResponseWrapper(correlationId, retrieveResponse))
      }
    }

    "return that wrapped error as-is" when {
      "the connectot returns an outbound error" in new Test {
        val someError = MtdError("SOME_CODE", "some message")
        val desResponse = ResponseWrapper(correlationId, OutboundError(someError))
        MockPensionChargesConnector.retrievePensions(request).returns(Future.successful(Left(desResponse)))

        await(service.retrievePensions(request)) shouldBe Left(ErrorWrapper(correlationId, someError))
      }
    }

    "service" should {
      "return a successful response" when {
        "a successful response is passed through" in new Test {
          MockPensionChargesConnector.retrievePensions(request)
            .returns(Future.successful(Right(ResponseWrapper(correlationId, retrieveResponse))))

          await(service.retrievePensions(request)) shouldBe Right(ResponseWrapper(correlationId, retrieveResponse))
        }
      }
      "map errors according to spec" when {
        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockPensionChargesConnector.retrievePensions(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

            await(service.retrievePensions(request)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val input = Seq(
          "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
          "INVALID_TAX_YEAR" -> TaxYearFormatError,
          "NO_DATA_FOUND" -> NotFoundError,
          "INVALID_CORRELATIONID" -> DownstreamError,
          "SERVER_ERROR" -> DownstreamError,
          "SERVICE_UNAVAILABLE" -> DownstreamError
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }
}
