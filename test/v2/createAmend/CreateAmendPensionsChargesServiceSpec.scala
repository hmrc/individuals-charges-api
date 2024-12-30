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

package v2.createAmend

import shared.controllers.EndpointLogContext
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors.{
  DownstreamErrorCode,
  DownstreamErrors,
  ErrorWrapper,
  InternalError,
  MtdError,
  NinoFormatError,
  OutboundError,
  RuleIncorrectOrEmptyBodyError,
  RuleTaxYearNotSupportedError,
  TaxYearFormatError
}
import shared.models.outcomes.ResponseWrapper
import shared.services.ServiceSpec
import uk.gov.hmrc.http.HeaderCarrier
import v2.createAmend.def1.fixture.Def1_CreateAmendPensionChargesFixture._
import v2.createAmend.def1.model.request.Def1_CreateAmendPensionChargesRequestData

import scala.concurrent.Future

class CreateAmendPensionsChargesServiceSpec extends ServiceSpec {

  val nino: Nino       = Nino("AA123456A")
  val taxYear: TaxYear = TaxYear.fromMtd("2020-21")

  private val request = Def1_CreateAmendPensionChargesRequestData(nino, taxYear, createAmendPensionChargesRequestBody)

  trait Test extends MockCreateAmendPensionChargesConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new CreateAmendPensionChargesService(mockCreateAmendPensionChargesConnector)
  }

  "Create & Amend Pension Charges" should {
    "return a valid response" when {
      "a valid request is supplied" in new Test {
        MockAmendPensionChargesConnector
          .amendPensionCharges(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        await(service.createAmendPensions(request)) shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }

    "return that wrapped error as-is" when {
      "the connector returns an outbound error" in new Test {
        val someError          = MtdError("SOME_CODE", "some message", BAD_REQUEST)
        val downstreamResponse = ResponseWrapper(correlationId, OutboundError(someError))
        MockAmendPensionChargesConnector.amendPensionCharges(request).returns(Future.successful(Left(downstreamResponse)))

        await(service.createAmendPensions(request)) shouldBe Left(ErrorWrapper(correlationId, someError))
      }
    }

    "unsuccessful" must {
      "map errors according to spec" when {

        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {

            MockAmendPensionChargesConnector
              .amendPensionCharges(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            await(service.createAmendPensions(request)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errors = Seq(
          "INVALID_TAXABLE_ENTITY_ID"    -> NinoFormatError,
          "INVALID_TAX_YEAR"             -> TaxYearFormatError,
          "INVALID_PAYLOAD"              -> RuleIncorrectOrEmptyBodyError,
          "INVALID_CORRELATIONID"        -> InternalError,
          "REDUCTION_TYPE_NOT_SPECIFIED" -> InternalError,
          "REDUCTION_NOT_SPECIFIED"      -> InternalError,
          "SERVER_ERROR"                 -> InternalError,
          "SERVICE_UNAVAILABLE"          -> InternalError
        )

        val extraTysErrors = Seq(
          "MISSING_ANNUAL_ALLOWANCE_REDUCTION" -> InternalError,
          "MISSING_TYPE_OF_REDUCTION"          -> InternalError,
          "TAX_YEAR_NOT_SUPPORTED"             -> RuleTaxYearNotSupportedError
        )

        (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
      }
    }
  }

}
