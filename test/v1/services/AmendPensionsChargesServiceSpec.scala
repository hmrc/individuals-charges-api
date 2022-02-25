/*
 * Copyright 2022 HM Revenue & Customs
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

import v1.data.AmendPensionChargesData._
import uk.gov.hmrc.http.HeaderCarrier
import v1.controllers.EndpointLogContext
import v1.mocks.connectors.MockPensionChargesConnector
import v1.models.domain.Nino
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.AmendPensionCharges.AmendPensionChargesRequest
import v1.models.request.DesTaxYear

import scala.concurrent.Future

class AmendPensionsChargesServiceSpec extends ServiceSpec {

  val nino: Nino = Nino("AA123456A")
  val taxYear: DesTaxYear = DesTaxYear("2020-21")

  private val request = AmendPensionChargesRequest(nino, taxYear, pensionCharges)

  trait Test extends MockPensionChargesConnector {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new AmendPensionChargesService(connector)
  }

  "Retrieve Pension Charges" should {
    "return a valid response" when {
      "a valid request is supplied" in new Test {
        MockPensionChargesConnector.amendPensionCharges(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        await(service.amendPensions(request)) shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }

    "return that wrapped error as-is" when {
      "the connectot returns an outbound error" in new Test {
        val someError = MtdError("SOME_CODE", "some message")
        val desResponse = ResponseWrapper(correlationId, OutboundError(someError))
        MockPensionChargesConnector.amendPensionCharges(request).returns(Future.successful(Left(desResponse)))

        await(service.amendPensions(request)) shouldBe Left(ErrorWrapper(correlationId, someError))
      }
    }

    "unsuccessful" must {
      "map errors according to spec" when {

        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockPensionChargesConnector.amendPensionCharges(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

            await(service.amendPensions(request)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val input = Seq(
          "INVALID_TAXABLE_ENTITY_ID"    -> NinoFormatError,
          "INVALID_TAX_YEAR"             -> TaxYearFormatError,
          "INVALID_PAYLOAD"              -> RuleIncorrectOrEmptyBodyError,
          "INVALID_CORRELATIONID"        -> DownstreamError,
          "REDUCTION_TYPE_NOT_SPECIFIED" -> DownstreamError,
          "REDUCTION_NOT_SPECIFIED"      -> DownstreamError,
          "SERVER_ERROR"                 -> DownstreamError,
          "SERVICE_UNAVAILABLE"          -> DownstreamError
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }
}
