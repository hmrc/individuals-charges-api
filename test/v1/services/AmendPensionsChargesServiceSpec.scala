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

import api.controllers.EndpointLogContext
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import mocks.MockAppConfig
import play.api.Configuration
import uk.gov.hmrc.http.HeaderCarrier
import v1.data.AmendPensionChargesData._
import v1.mocks.connectors.MockAmendPensionChargesConnector
import v1.models.request.AmendPensionCharges.AmendPensionChargesRequest

import scala.concurrent.Future

class AmendPensionsChargesServiceSpec extends ServiceSpec {

  val nino: Nino       = Nino("AA123456A")
  val taxYear: TaxYear = TaxYear.fromMtd("2020-21")

  private val request = AmendPensionChargesRequest(nino, taxYear, pensionChargesCl102FieldsInTaxCharges)

  trait Test extends MockAmendPensionChargesConnector with MockAppConfig {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new AmendPensionChargesService(mockAmendPensionChargesConnector, mockAppConfig)
  }

  trait Cl102Enabled extends Test {
    MockAppConfig.featureSwitches.returns(Configuration("cl102.enabled" -> true)).anyNumberOfTimes()
  }

  trait Cl102Disabled extends Test {
    MockAppConfig.featureSwitches.returns(Configuration("cl102.enabled" -> false)).anyNumberOfTimes()
  }

  "Amend Pension Charges" should {
    "return a valid response" when {
      "a valid request is supplied" in new Cl102Disabled {
        MockAmendPensionChargesConnector
          .amendPensionCharges(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        await(service.amendPensions(request)) shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }

    "return that wrapped error as-is" when {
      "the connector returns an outbound error" in new Cl102Disabled {
        val someError          = MtdError("SOME_CODE", "some message", BAD_REQUEST)
        val downstreamResponse = ResponseWrapper(correlationId, OutboundError(someError))
        MockAmendPensionChargesConnector.amendPensionCharges(request).returns(Future.successful(Left(downstreamResponse)))

        await(service.amendPensions(request)) shouldBe Left(ErrorWrapper(correlationId, someError))
      }
    }

    "unsuccessful" must {
      "map errors according to spec" when {

        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Cl102Disabled {

            MockAmendPensionChargesConnector
              .amendPensionCharges(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            await(service.amendPensions(request)) shouldBe Left(ErrorWrapper(correlationId, error))
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

    "cl102 is enabled" must {
      "cl102 field updates cannot be performed" when {
        "internal server error is returned from the service" in new Cl102Enabled {
          val requestMissingPensionContributions = AmendPensionChargesRequest(nino, taxYear, pensionChargesPensionContributionsMissing)

          await(service.amendPensions(requestMissingPensionContributions)) shouldBe Left(ErrorWrapper(correlationId, InternalError))
        }
      }

      "cl102 field updates are completed successfully" when {
        "updated request passed onto connector" in new Cl102Enabled {
          val modifiedRequest = AmendPensionChargesRequest(nino, taxYear, pensionChargesCl102FieldsInPensionContributions)

          MockAmendPensionChargesConnector
            .amendPensionCharges(modifiedRequest)
            .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

          await(service.amendPensions(request)) shouldBe Right(ResponseWrapper(correlationId, ()))
        }
      }
    }
  }

}
