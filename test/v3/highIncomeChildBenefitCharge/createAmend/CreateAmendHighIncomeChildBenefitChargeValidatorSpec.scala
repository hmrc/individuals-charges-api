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

package v3.highIncomeChildBenefitCharge.createAmend

import play.api.libs.json._
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.utils.JsonErrorValidators
import shared.utils.UnitSpec
import v3.highIncomeChildBenefitCharge.createAmend.fixture.CreateAmendHighIncomeChildBenefitChargeFixtures._
import v3.highIncomeChildBenefitCharge.createAmend.models.request.CreateAmendHighIncomeChildBenefitChargeRequest

class CreateAmendHighIncomeChildBenefitChargeValidatorSpec extends UnitSpec with JsonErrorValidators {

  private implicit val correlationId: String = "1234"

  private val validNino: String         = "AA123456A"
  private val validTaxYear: String      = "2025-26"

  private val parsedNino: Nino                 = Nino(validNino)
  private val parsedTaxYear: TaxYear           = TaxYear.fromMtd(validTaxYear)

  private def validator(nino: String,
                        taxYear: String,
                        body: JsValue): CreateAmendHighIncomeChildBenefitChargeValidator =
    new CreateAmendHighIncomeChildBenefitChargeValidator(nino, taxYear, body)

  "running a validation" should {
    "return no errors" when {
      "a full valid request is supplied" in {
        val result: Either[ErrorWrapper, CreateAmendHighIncomeChildBenefitChargeRequest] =
          validator(validNino, validTaxYear, validFullRequestBodyJson).validateAndWrapResult()

        result shouldBe Right(CreateAmendHighIncomeChildBenefitChargeRequest(parsedNino, parsedTaxYear, fullRequestBodyModel))
      }

      "a minimum valid request is supplied" in {
        val result: Either[ErrorWrapper, CreateAmendHighIncomeChildBenefitChargeRequest] =
          validator(validNino, validTaxYear, validMinimumRequestBodyJson).validateAndWrapResult()

        result shouldBe Right(CreateAmendHighIncomeChildBenefitChargeRequest(parsedNino, parsedTaxYear, minimumRequestBodyModel))
      }
    }

    "return NinoFormatError error" when {
      "an invalid nino is supplied" in {
        val result: Either[ErrorWrapper, CreateAmendHighIncomeChildBenefitChargeRequest] =
          validator("A12344A", validTaxYear, validFullRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }
    }

    "return TaxYearFormatError error" when {
      "an invalid tax year is supplied" in {
        val result: Either[ErrorWrapper, CreateAmendHighIncomeChildBenefitChargeRequest] =
          validator(validNino, "20256", validFullRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }
    }

    "return RuleTaxYearRangeInvalidError error" when {
      "an invalid tax year range is supplied" in {
        val result: Either[ErrorWrapper, CreateAmendHighIncomeChildBenefitChargeRequest] =
          validator(validNino, "2025-27", validFullRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }
    }

    "return RuleTaxYearNotSupportedError error" when {
      "an unsupported tax year is supplied" in {
        val result: Either[ErrorWrapper, CreateAmendHighIncomeChildBenefitChargeRequest] =
          validator(validNino, "2024-25", validFullRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }
    }


    "return RuleIncorrectOrEmptyBodyError error" when {
      "passed an empty body" in {
        val result: Either[ErrorWrapper, CreateAmendHighIncomeChildBenefitChargeRequest] =
          validator(validNino, validTaxYear, JsObject.empty).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }

      "passed a body with a missing mandatory field" in {
        val result: Either[ErrorWrapper, CreateAmendHighIncomeChildBenefitChargeRequest] =
          validator(validNino, validTaxYear, validFullRequestBodyJson.removeProperty("/amountOfChildBenefitReceived")).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/amountOfChildBenefitReceived")))
      }

      validFullRequestBodyJson.as[JsObject].fields.foreach { case (field, _) =>
        s"passed a body with an incorrect type for field $field" in {
          val invalidJson: JsValue = validFullRequestBodyJson.update(s"/$field", JsObject.empty)

          val result: Either[ErrorWrapper, CreateAmendHighIncomeChildBenefitChargeRequest] =
            validator(validNino, validTaxYear, invalidJson).validateAndWrapResult()

          result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath(s"/$field")))
        }
      }
    }

    "return ValueFormatError error" when {

      val testCases = Seq(
        (
          "amount of child Benefit received is incorrectly formatted",
          __ \ "amountOfChildBenefitReceived",
          JsNumber(18.999),
          ValueFormatError.forPathAndRange("/amountOfChildBenefitReceived", "0", "99999999999.99")
        ),
        (
          "number of children is incorrectly formatted",
          __ \ "numberOfChildren",
          JsNumber(121),
          ValueFormatError.forIntegerPathAndRange("/numberOfChildren", "1", "99")
        )
      )

      testCases.foreach { case (description, path, invalidValue, expectedError) =>
        s"passed a body where $description" in {
          val invalidJson: JsValue = validFullRequestBodyJson.transform(__.json.update(path.json.put(invalidValue))).get

          val result: Either[ErrorWrapper, CreateAmendHighIncomeChildBenefitChargeRequest] =
            validator(validNino, validTaxYear, invalidJson).validateAndWrapResult()

          result shouldBe Left(ErrorWrapper(correlationId, expectedError))
        }
      }
    }


    "return DateCeasedFormatError error" when {
      "passed a body with an incorrectly formatted date ceased" in {
        val invalidJson: JsValue = validFullRequestBodyJson.update("/dateCeased", JsString("2025"))

        val result: Either[ErrorWrapper, CreateAmendHighIncomeChildBenefitChargeRequest] =
          validator(validNino, validTaxYear, invalidJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, DateCeasedFormatError))
      }
    }


    "return RuleDateCeasedError error" when {
      "passed a body with a date ceased outside the supplied tax year" in {
        val invalidJson: JsValue = validFullRequestBodyJson.update("/dateCeased", JsString("2025-04-05"))

        val result: Either[ErrorWrapper, CreateAmendHighIncomeChildBenefitChargeRequest] =
          validator(validNino, validTaxYear, invalidJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleDateCeasedError))
      }
    }

    "return multiple errors" when {
      "request supplied has multiple errors" in {
        val result: Either[ErrorWrapper, CreateAmendHighIncomeChildBenefitChargeRequest] =
          validator("A12344A", "20256", validFullRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BadRequestError, Some(List(NinoFormatError, TaxYearFormatError))))
      }
    }
  }

}
