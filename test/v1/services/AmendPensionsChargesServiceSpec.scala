/*
 * Copyright 2020 HM Revenue & Customs
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

import data.AmendPensionChargesData._
import uk.gov.hmrc.domain.Nino
import v1.mocks.connectors.MockPensionChargesConnector
import v1.models.errors._
import v1.models.outcomes.DesResponse
import v1.models.requestData.{DesTaxYear, AmendPensionChargesRequest}

import scala.concurrent.Future

class AmendPensionsChargesServiceSpec extends ServiceSpec {

  val nino: Nino = Nino("AA123456A")
  val taxYear: DesTaxYear = DesTaxYear("2020-21")
  val correlationId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  private val request = AmendPensionChargesRequest(nino, taxYear, pensionCharges)

  trait Test extends MockPensionChargesConnector {
    val service = new AmendPensionChargesService(connector)
  }

  "Retrieve Pension Charges" should {
    "return a valid response" when {
      "a valid request is supplied" in new Test {
        val desResponse = DesResponse(correlationId, ())
        MockPensionChargesConnector.amendPensionCharges(request)
          .returns(Future.successful(Right(desResponse)))

        await(service.amendPensions(request)) shouldBe Right(desResponse)
      }
    }

    "return that wrapped error as-is" when {
      "the connectot returns an outbound error" in new Test {
        val someError = MtdError("SOME_CODE", "some message")
        val desResponse = DesResponse(correlationId, OutboundError(someError))
        MockPensionChargesConnector.amendPensionCharges(request).returns(Future.successful(Left(desResponse)))

        await(service.amendPensions(request)) shouldBe Left(ErrorWrapper(Some(correlationId), Seq(someError)))
      }
    }

    "return a downstream error" when {
      "the connector call returns a single downstream error" in new Test {
        val desResponse = DesResponse(correlationId, SingleError(DownstreamError))
        val expected = ErrorWrapper(Some(correlationId), Seq(DownstreamError))
        MockPensionChargesConnector.amendPensionCharges(request).returns(Future.successful(Left(desResponse)))

        await(service.amendPensions(request)) shouldBe Left(expected)
      }

      "the connector call returns multiple errors including a downstream error" in new Test {
        val desResponse = DesResponse(correlationId, MultipleErrors(Seq(NinoFormatError, DownstreamError)))
        val expected    = ErrorWrapper(Some(correlationId), Seq(DownstreamError))
        MockPensionChargesConnector.amendPensionCharges(request).returns(Future.successful(Left(desResponse)))

        await(service.amendPensions(request)) shouldBe Left(expected)
      }
    }

    Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NotFoundError,
      "INVALID_TAX_YEAR"          -> TaxYearFormatError,
      "INVALID_PAYLOAD"           -> RuleIncorrectOrEmptyBodyError,
      "INVALID_CORRELATIONID"     -> DownstreamError,
      "SERVER_ERROR"              -> DownstreamError,
      "SERVICE_UNAVAILABLE"       -> DownstreamError
    ).foreach {
      case (k, v) =>
        s"return a ${v.code} error" when {
          s"the connector call returns $k" in new Test {
            MockPensionChargesConnector.amendPensionCharges(request)
              .returns(Future.successful(Left(DesResponse(correlationId, SingleError(MtdError(k, "doesn't matter"))))))

            await(service.amendPensions(request)) shouldBe Left(ErrorWrapper(Some(correlationId), Seq(v)))
          }
        }
    }

  }
}
