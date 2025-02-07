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

package v3.retrieve

import shared.controllers.EndpointLogContext
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.ServiceSpec
import uk.gov.hmrc.http.HeaderCarrier
import v3.retrieve.def1.fixture.RetrievePensionChargesFixture._
import v3.retrieve.def1.model.request.Def1_RetrievePensionChargesRequestData

import scala.concurrent.Future

class RetrievePensionsChargesServiceSpec extends ServiceSpec {

  val nino: Nino       = Nino("AA123456A")
  val taxYear: TaxYear = TaxYear.fromMtd("2020-21")

  private val request = Def1_RetrievePensionChargesRequestData(nino, taxYear)

  trait Test extends MockRetrievePensionChargesConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")
    val service                                 = new RetrievePensionChargesService(mockRetrievePensionChargesConnector)
  }

  "Retrieve Pension Charges" should {
    "return a valid response" when {
      "a valid request is supplied" in new Test {
        MockRetrievePensionChargesConnector
          .retrievePensions(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, retrieveResponse))))

        await(service.retrievePensions(request)) shouldBe Right(ResponseWrapper(correlationId, retrieveResponse))
      }
    }

    "return that wrapped error as-is" when {
      "the connector returns an outbound error" in new Test {
        val someError   = MtdError("SOME_CODE", "some message", BAD_REQUEST)
        val desResponse = ResponseWrapper(correlationId, OutboundError(someError))
        MockRetrievePensionChargesConnector.retrievePensions(request).returns(Future.successful(Left(desResponse)))

        await(service.retrievePensions(request)) shouldBe Left(ErrorWrapper(correlationId, someError))
      }
    }

    "service" should {
      "return a successful response" when {
        "a successful response is passed through" in new Test {
          MockRetrievePensionChargesConnector
            .retrievePensions(request)
            .returns(Future.successful(Right(ResponseWrapper(correlationId, retrieveResponse))))

          await(service.retrievePensions(request)) shouldBe Right(ResponseWrapper(correlationId, retrieveResponse))
        }
      }
      "map errors according to spec" when {
        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockRetrievePensionChargesConnector
              .retrievePensions(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(desErrorCode))))))

            await(service.retrievePensions(request)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errors = Seq(
          "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
          "INVALID_TAX_YEAR"          -> TaxYearFormatError,
          "NO_DATA_FOUND"             -> NotFoundError,
          "INVALID_CORRELATIONID"     -> InternalError,
          "SERVER_ERROR"              -> InternalError,
          "SERVICE_UNAVAILABLE"       -> InternalError
        )

        val extraTysErrors = Seq(
          "NOT_FOUND"              -> NotFoundError,
          "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError
        )

        (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
      }
    }
  }

}
