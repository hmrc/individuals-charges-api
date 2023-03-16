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

import anyVersion.models.request.retrievePensionCharges.RetrievePensionChargesRequest
import api.controllers.EndpointLogContext
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import mocks.MockAppConfig
import play.api.Configuration
import uk.gov.hmrc.http.HeaderCarrier
import v1.data.RetrievePensionChargesData._
import v1.mocks.connectors.MockRetrievePensionChargesConnector

import scala.concurrent.Future

class RetrievePensionsChargesServiceSpec extends ServiceSpec {

  val nino: Nino       = Nino("AA123456A")
  val taxYear: TaxYear = TaxYear.fromMtd("2020-21")

  private val request = RetrievePensionChargesRequest(nino, taxYear)

  trait Test extends MockRetrievePensionChargesConnector with MockAppConfig {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")
    val service                                 = new RetrievePensionChargesService(mockRetrievePensionChargesConnector, mockAppConfig)
  }

  trait Cl102Enabled extends Test {
    MockAppConfig.featureSwitches.returns(Configuration("cl102.enabled" -> true)).anyNumberOfTimes()
  }

  trait Cl102Disabled extends Test {
    MockAppConfig.featureSwitches.returns(Configuration("cl102.enabled" -> false)).anyNumberOfTimes()
  }

  "Retrieve Pension Charges" should {
    "return a valid response" when {
      "a valid request is supplied" in new Cl102Disabled {
        MockRetrievePensionChargesConnector
          .retrievePensions(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, retrieveResponseCl102FieldsInTaxCharges))))

        await(service.retrievePensions(request)) shouldBe Right(ResponseWrapper(correlationId, retrieveResponseCl102FieldsInTaxCharges))
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
        "a successful response is passed through" in new Cl102Disabled {
          MockRetrievePensionChargesConnector
            .retrievePensions(request)
            .returns(Future.successful(Right(ResponseWrapper(correlationId, retrieveResponseCl102FieldsInTaxCharges))))

          await(service.retrievePensions(request)) shouldBe Right(ResponseWrapper(correlationId, retrieveResponseCl102FieldsInTaxCharges))
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
    "cl102 is enabled" must {
      "cl102 fields exist in pension contributions" when {
        "a successful response is passed through with updated fields" in new Cl102Enabled {
          MockRetrievePensionChargesConnector
            .retrievePensions(request)
            .returns(Future.successful(Right(ResponseWrapper(correlationId, retrieveResponseCl102FieldsInPensionContributions))))

          await(service.retrievePensions(request)) shouldBe Right(ResponseWrapper(correlationId, retrieveResponseCl102FieldsInTaxCharges))
        }
      }

      "isAnnualAllowanceReduced missing" when {
        "an internal server error is returned" in new Cl102Enabled {
          val responseWithoutIsAnnualAllowanceReduced = retrieveResponseCl102FieldsInPensionContributions.copy(pensionContributions =
            Some(pensionContributionsWithCl102Fields.copy(isAnnualAllowanceReduced = None)))

          MockRetrievePensionChargesConnector
            .retrievePensions(request)
            .returns(Future.successful(Right(ResponseWrapper(correlationId, responseWithoutIsAnnualAllowanceReduced))))

          await(service.retrievePensions(request)) shouldBe Left(ErrorWrapper(correlationId, InternalError))
        }
      }
    }

  }

}
