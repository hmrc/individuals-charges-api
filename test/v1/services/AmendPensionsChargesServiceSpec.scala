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

package v1.services

import api.models.errors.{DownstreamErrorCode, DownstreamErrors, ErrorWrapper, MtdError, NinoFormatError, OutboundError, RuleIncorrectOrEmptyBodyError, RuleTaxYearNotSupportedError, StandardDownstreamError, TaxYearFormatError}
import v1.data.AmendPensionChargesData._
import uk.gov.hmrc.http.HeaderCarrier
import v1.controllers.EndpointLogContext
import v1.mocks.connectors.MockPensionChargesConnector
import v1.models.domain.Nino
import api.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.AmendPensionCharges.AmendPensionChargesRequest
import v1.models.request.TaxYear

import scala.concurrent.Future

class AmendPensionsChargesServiceSpec extends ServiceSpec {

  val nino: Nino       = Nino("AA123456A")
  val taxYear: TaxYear = TaxYear.fromMtd("2020-21")

  private val request = AmendPensionChargesRequest(nino, taxYear, pensionCharges)

  trait Test extends MockPensionChargesConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new AmendPensionChargesService(connector)
  }

  "Retrieve Pension Charges" should {
    "return a valid response" when {
      "a valid request is supplied" in new Test {
        MockPensionChargesConnector
          .amendPensionCharges(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        await(service.amendPensions(request)) shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }

    "return that wrapped error as-is" when {
      "the connector returns an outbound error" in new Test {
        val someError   = MtdError("SOME_CODE", "some message")
        val downstreamResponse = ResponseWrapper(correlationId, OutboundError(someError))
        MockPensionChargesConnector.amendPensionCharges(request).returns(Future.successful(Left(downstreamResponse)))

        await(service.amendPensions(request)) shouldBe Left(ErrorWrapper(correlationId, someError))
      }
    }

    "unsuccessful" must {
      "map errors according to spec" when {

        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {

            MockPensionChargesConnector
              .amendPensionCharges(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            await(service.amendPensions(request)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errors = Seq(
          "INVALID_TAXABLE_ENTITY_ID"    -> NinoFormatError,
          "INVALID_TAX_YEAR"             -> TaxYearFormatError,
          "INVALID_PAYLOAD"              -> RuleIncorrectOrEmptyBodyError,
          "INVALID_CORRELATIONID"        -> StandardDownstreamError,
          "REDUCTION_TYPE_NOT_SPECIFIED" -> StandardDownstreamError,
          "REDUCTION_NOT_SPECIFIED"      -> StandardDownstreamError,
          "SERVER_ERROR"                 -> StandardDownstreamError,
          "SERVICE_UNAVAILABLE"          -> StandardDownstreamError
        )

        val extraTysErrors = Seq(
          "MISSING_ANNUAL_ALLOWANCE_REDUCTION" -> StandardDownstreamError,
          "MISSING_TYPE_OF_REDUCTION"          -> StandardDownstreamError,
          "TAX_YEAR_NOT_SUPPORTED"             -> RuleTaxYearNotSupportedError
        )

        (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
      }
    }
  }

}
