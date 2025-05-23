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

package v3.pensionCharges.retrieve.def1

import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.utils.UnitSpec
import v3.pensionCharges.retrieve.def1.model.Def1_RetrievePensionChargesValidator
import v3.pensionCharges.retrieve.def1.model.request.Def1_RetrievePensionChargesRequestData
import v3.pensionCharges.retrieve.model.request.RetrievePensionChargesRequestData

class Def1_RetrievePensionChargesValidatorSpec extends UnitSpec {
  private implicit val correlationId: String = "1234"

  private val validNino    = "AA123456A"
  private val validTaxYear = "2021-22"

  private val parsedNino    = Nino(validNino)
  private val parsedTaxYear = TaxYear.fromMtd(validTaxYear)

  private def validator(nino: String, taxYear: String) = new Def1_RetrievePensionChargesValidator(nino, taxYear)

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {
        val result: Either[ErrorWrapper, RetrievePensionChargesRequestData] =
          validator(validNino, validTaxYear).validateAndWrapResult()

        result shouldBe Right(Def1_RetrievePensionChargesRequestData(parsedNino, parsedTaxYear))
      }
    }

    "should return a single error" when {
      "an invalid nino is supplied" in {
        val result: Either[ErrorWrapper, RetrievePensionChargesRequestData] =
          validator("invalidNino", validTaxYear).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "an incorrectly formatted taxYear is supplied" in {
        val result: Either[ErrorWrapper, RetrievePensionChargesRequestData] =
          validator(validNino, "202122").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }

      "an invalid tax year range is supplied" in {
        val result: Either[ErrorWrapper, RetrievePensionChargesRequestData] =
          validator(validNino, "2020-22").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }

      "an invalid tax year, before the minimum, is supplied" in {
        val result: Either[ErrorWrapper, RetrievePensionChargesRequestData] =
          validator(validNino, "2020-21").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }

      "an invalid tax year, after the maximum, is supplied" in {
        val result: Either[ErrorWrapper, RetrievePensionChargesRequestData] =
          validator(validNino, "2027-28").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }
    }

    "return multiple errors" when {
      "request supplied has multiple errors" in {
        val result: Either[ErrorWrapper, RetrievePensionChargesRequestData] =
          validator("invalidNino", "invalidTaxYear").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BadRequestError, Some(List(NinoFormatError, TaxYearFormatError))))
      }
    }
  }

}
