/*
 * Copyright 2025 HM Revenue & Customs
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

package v3.winterFuelPayment.createAmend

import play.api.libs.json.*
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors.*
import shared.models.utils.JsonErrorValidators
import shared.utils.UnitSpec
import v3.winterFuelPayment.createAmend.fixture.CreateAmendWinterFuelPaymentFixtures.*
import v3.winterFuelPayment.createAmend.models.request.CreateAmendWinterFuelPaymentRequestData

class CreateAmendWinterFuelPaymentValidatorSpec extends UnitSpec with JsonErrorValidators {

  private implicit val correlationId: String = "1234"

  private val validNino: String    = "AA123456A"
  private val validTaxYear: String = "2026-27"

  private val parsedNino: Nino       = Nino(validNino)
  private val parsedTaxYear: TaxYear = TaxYear.fromMtd(validTaxYear)

  private def validator(nino: String, taxYear: String, body: JsValue, temporalValidationEnabled: Boolean = false): CreateAmendWinterFuelPaymentValidator =
    new CreateAmendWinterFuelPaymentValidator(nino, taxYear, body, temporalValidationEnabled)

  "running a validation" should {
    "return no errors" when {
      "a full valid request is supplied" in {
        val result: Either[ErrorWrapper, CreateAmendWinterFuelPaymentRequestData] =
          validator(validNino, validTaxYear, validRequestBodyJson).validateAndWrapResult()

        result shouldBe Right(CreateAmendWinterFuelPaymentRequestData(parsedNino, parsedTaxYear, requestBodyModel))
      }
    }

    "return NinoFormatError error" when {
      "an invalid nino is supplied" in {
        val result: Either[ErrorWrapper, CreateAmendWinterFuelPaymentRequestData] =
          validator("A12344A", validTaxYear, validRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }
    }

    "return TaxYearFormatError error" when {
      "an invalid tax year is supplied" in {
        val result: Either[ErrorWrapper, CreateAmendWinterFuelPaymentRequestData] =
          validator(validNino, "20256", validRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }
    }

    "return RuleTaxYearRangeInvalidError error" when {
      "an invalid tax year range is supplied" in {
        val result: Either[ErrorWrapper, CreateAmendWinterFuelPaymentRequestData] =
          validator(validNino, "2025-27", validRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }
    }

    "return RuleTaxYearNotSupportedError error" when {
      "an unsupported tax year is supplied" in {
        val result: Either[ErrorWrapper, CreateAmendWinterFuelPaymentRequestData] =
          validator(validNino, "2024-25", validRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }
    }

    "return RuleTaxYearNotEndedError error" when {
      "the tax year has not ended" in {
        val result: Either[ErrorWrapper, CreateAmendWinterFuelPaymentRequestData] =
          validator(validNino, TaxYear.currentTaxYear.asMtd, validRequestBodyJson, true).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearNotEndedError))
      }
    }

    "return RuleIncorrectOrEmptyBodyError error" when {
      "passed an empty body" in {
        val result: Either[ErrorWrapper, CreateAmendWinterFuelPaymentRequestData] =
          validator(validNino, validTaxYear, JsObject.empty).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }
    }

    "return RuleIncorrectOrEmptyBodyError error" when {
      "passed a value for winter fuel payment of incorrect type" in {
        
        val invalidJson: JsValue = validRequestBodyJson.update(JsPath \ "winterFuelPayment", JsString("invalid"))
        
        val result: Either[ErrorWrapper, CreateAmendWinterFuelPaymentRequestData] =
          validator(validNino, validTaxYear, invalidJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/winterFuelPayment")))
      }
    }
    
    "return ValueFormatError" when {
      "passed a value for winter fuel payment exceeding max value" in {
        val invalidJson: JsValue = validRequestBodyJson.update(JsPath \ "winterFuelPayment", JsNumber(123.123))

        val result: Either[ErrorWrapper, CreateAmendWinterFuelPaymentRequestData] =
          validator(validNino, validTaxYear, invalidJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, ValueFormatError.withPath("/winterFuelPayment")))
      }
    }
    

    "return multiple errors" when {
      "request supplied has multiple errors" in {
        val result: Either[ErrorWrapper, CreateAmendWinterFuelPaymentRequestData] =
          validator("A12344A", "20256", validRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BadRequestError, Some(List(NinoFormatError, TaxYearFormatError))))
      }
    }
  }

}
