/*
 * Copyright 2026 HM Revenue & Customs
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

package v3.winterFuelPayment.delete

import shared.models.domain.{Nino, TaxYear}
import shared.models.errors.*
import shared.utils.UnitSpec
import v3.winterFuelPayment.delete.model.request.DeleteWinterFuelPaymentRequestData

class DeleteWinterFuelPaymentValidatorFactorySpec extends UnitSpec {
  private implicit val correlationId: String = "1234"

  private val validNino    = "AA123456A"
  private val validTaxYear = "2026-27"

  private val parsedNino    = Nino(validNino)
  private val parsedTaxYear = TaxYear.fromMtd(validTaxYear)

  private val validatorFactory = new DeleteWinterFuelPaymentValidatorFactory

  private def validator(nino: String, taxYear: String) = validatorFactory.validator(nino, taxYear)

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {
        val result: Either[ErrorWrapper, DeleteWinterFuelPaymentRequestData] =
          validator(validNino, validTaxYear).validateAndWrapResult()

        result shouldBe Right(DeleteWinterFuelPaymentRequestData(parsedNino, parsedTaxYear))
      }
    }

    "return NinoFormatError" when {
      "an invalid nino is supplied" in {
        val result: Either[ErrorWrapper, DeleteWinterFuelPaymentRequestData] =
          validator("invalidNino", validTaxYear).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }
    }

    "return TaxYearFormatError" when {
      "an incorrectly formatted taxYear is supplied" in {
        val result: Either[ErrorWrapper, DeleteWinterFuelPaymentRequestData] =
          validator(validNino, "202122").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }
    }

    "return RuleTaxYearRangeInvalidError" when {
      "an invalid tax year range is supplied" in {
        val result: Either[ErrorWrapper, DeleteWinterFuelPaymentRequestData] =
          validator(validNino, "2020-22").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }
    }

    "return RuleTaxYearNotSupportedError" when {
      "an unsupported tax year is supplied" in {
        val result: Either[ErrorWrapper, DeleteWinterFuelPaymentRequestData] =
          validator(validNino, "2025-26").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }
    }
  }

}
