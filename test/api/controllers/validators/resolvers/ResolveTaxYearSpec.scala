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

package api.controllers.validators.resolvers

import api.models.domain.TaxYear
import api.models.errors.*
import api.utils.UnitSpec
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}

class ResolveTaxYearSpec extends UnitSpec with ResolverSupport {

  "ResolveTaxYear" should {
    "return no errors" when {
      val validTaxYear = "2018-19"

      "given a valid tax year" in {
        val result: Validated[Seq[MtdError], TaxYear] = ResolveTaxYear(validTaxYear)
        result shouldBe Valid(TaxYear.fromMtd(validTaxYear))
      }

      "given a valid tax year in an Option" in {
        val result: Validated[Seq[MtdError], Option[TaxYear]] = ResolveTaxYear(Option(validTaxYear))
        result shouldBe Valid(Some(TaxYear.fromMtd(validTaxYear)))
      }

      "given an empty Option" in {
        val result: Validated[Seq[MtdError], Option[TaxYear]] = ResolveTaxYear(None)
        result shouldBe Valid(None)
      }
    }

    "return an error" when {
      "given an invalid tax year format" in {
        ResolveTaxYear("2019") shouldBe Invalid(List(TaxYearFormatError))
      }

      "given a tax year string in which the range is greater than 1 year" in {
        ResolveTaxYear("2017-19") shouldBe Invalid(List(RuleTaxYearRangeInvalidError))
      }

      "the end year is before the start year" in {
        ResolveTaxYear("2018-17") shouldBe Invalid(List(RuleTaxYearRangeInvalidError))
      }

      "the start and end years are the same" in {
        ResolveTaxYear("2017-17") shouldBe Invalid(List(RuleTaxYearRangeInvalidError))
      }

      "the tax year is bad" in {
        ResolveTaxYear("20177-17") shouldBe Invalid(List(TaxYearFormatError))
      }
    }
  }

  "ResolveDetailedTaxYear using the default minimum-year behaviour" should {
    val minimumTaxYear: TaxYear = TaxYear.fromMtd("2021-22")
    val currentTaxYear: TaxYear = TaxYear.currentTaxYear

    def resolver(allowIncompleteTaxYear: Boolean = true): ResolveDetailedTaxYear = ResolveDetailedTaxYear(
      minimumTaxYear = minimumTaxYear,
      allowIncompleteTaxYear = allowIncompleteTaxYear
    )

    "return no errors" when {
      "given the minimum allowed tax year" in {
        val result: Validated[Seq[MtdError], TaxYear] = resolver()("2021-22")
        result shouldBe Valid(minimumTaxYear)
      }

      "given an incomplete tax year but incomplete years are allowed" in {
        val result: Validated[Seq[MtdError], TaxYear] = resolver()(currentTaxYear.asMtd)
        result shouldBe Valid(currentTaxYear)
      }
    }

    "return RuleTaxYearNotSupportedError" when {
      "given the tax year is before the minimum tax year" in {
        val result: Validated[Seq[MtdError], TaxYear] = resolver()("2020-21")
        result shouldBe Invalid(List(RuleTaxYearNotSupportedError))
      }
    }

    "return RuleTaxYearNotEndedError" when {
      "given an incomplete tax year and incomplete years are not allowed" in {
        val result: Validated[Seq[MtdError], TaxYear] = resolver(false)(currentTaxYear.asMtd)
        result shouldBe Invalid(List(RuleTaxYearNotEndedError))
      }
    }
  }

  "ResolveDetailedTaxYear using custom minimum errors" should {
    val minimumTaxYear = TaxYear.fromMtd("2021-22")

    val notSupportedError = NotFoundError.withPath("/notSupported")
    val formatError       = NinoFormatError.withPath("/formatError")
    val rangeError        = BadRequestError.withPath("/rangeError")

    val resolver = ResolveDetailedTaxYear(
      minimumTaxYear = minimumTaxYear,
      minError = notSupportedError,
      formatError = formatError,
      rangeError = rangeError
    )

    "return no errors" when {
      "given the minimum allowed tax year" in {
        val result: Validated[Seq[MtdError], TaxYear] = resolver("2021-22")
        result shouldBe Valid(minimumTaxYear)
      }
    }

    "return the custom error" when {
      "given a tax year before the minimum tax year" in {
        val result: Validated[Seq[MtdError], TaxYear] = resolver("2020-21")
        result shouldBe Invalid(List(notSupportedError))
      }

      "given a badly formatted tax year" in {
        val result: Validated[Seq[MtdError], TaxYear] = resolver("not-a-tax-year")
        result shouldBe Invalid(List(formatError))
      }

      "given a tax year with an invalid range" in {
        val result: Validated[Seq[MtdError], TaxYear] = resolver("2024-26")
        result shouldBe Invalid(List(rangeError))
      }
    }
  }

  "ResolveDetailedTaxYear" should {
    "return no errors" when {
      "given the maximum allowed tax year" in {
        val maximumTaxYear = TaxYear.fromMtd("2024-25")
        val resolver = ResolveDetailedTaxYear(
          minimumTaxYear = TaxYear.fromMtd("2021-22"),
          maximumTaxYear = Some(maximumTaxYear)
        )

        val result: Validated[Seq[MtdError], TaxYear] = resolver("2024-25")
        result shouldBe Valid(maximumTaxYear)
      }

      "given the minimum allowed tax year" in {
        val minimumTaxYear = TaxYear.fromMtd("2021-22")
        val resolver = ResolveDetailedTaxYear(
          minimumTaxYear = minimumTaxYear,
          maximumTaxYear = Some(TaxYear.fromMtd("2024-25"))
        )

        val result: Validated[Seq[MtdError], TaxYear] = resolver("2021-22")
        result shouldBe Valid(minimumTaxYear)
      }

      "given a tax year between the minimum and maximum" in {
        val resolver = ResolveDetailedTaxYear(
          minimumTaxYear = TaxYear.fromMtd("2021-22"),
          maximumTaxYear = Some(TaxYear.fromMtd("2024-25"))
        )

        val result: Validated[Seq[MtdError], TaxYear] = resolver("2023-24")
        result shouldBe Valid(TaxYear.fromMtd("2023-24"))
      }

      "given an incomplete tax year but incomplete years are allowed" in {
        val currentTaxYear = TaxYear.currentTaxYear
        val resolver = ResolveDetailedTaxYear(
          minimumTaxYear = TaxYear.fromMtd("2021-22")
        )

        val result: Validated[Seq[MtdError], TaxYear] = resolver(currentTaxYear.asMtd)
        result shouldBe Valid(currentTaxYear)
      }

      "given a valid tax year that's above or equal to TaxYear.tysTaxYear" in {
        val validTaxYear = "2023-24"
        val resolver = ResolveDetailedTaxYear(
          minimumTaxYear = TaxYear.tysTaxYear,
          minError = InvalidTaxYearParameterError
        )

        val result: Validated[Seq[MtdError], TaxYear] = resolver(validTaxYear)
        result shouldBe Valid(TaxYear.fromMtd(validTaxYear))
      }
    }

    "return RuleTaxYearNotSupportedError" when {
      "given the tax year is after the maximum tax year" in {
        val resolver = ResolveDetailedTaxYear(
          minimumTaxYear = TaxYear.fromMtd("2021-22"),
          maximumTaxYear = Some(TaxYear.fromMtd("2024-25"))
        )

        val result: Validated[Seq[MtdError], TaxYear] = resolver("2025-26")
        result shouldBe Invalid(List(RuleTaxYearNotSupportedError))
      }

      "given a tax year earlier than the minimum" in {
        val resolver = ResolveDetailedTaxYear(
          minimumTaxYear = TaxYear.fromMtd("2021-22"),
          maximumTaxYear = Some(TaxYear.fromMtd("2024-25"))
        )

        val result: Validated[Seq[MtdError], TaxYear] = resolver("2020-21")
        result shouldBe Invalid(List(RuleTaxYearNotSupportedError))
      }
    }

    "return the expected custom error" when {
      val minimumTaxYear = TaxYear.fromMtd("2021-22")
      val maximumTaxYear = TaxYear.fromMtd("2024-25")
      val resolver = ResolveDetailedTaxYear(
        minimumTaxYear = minimumTaxYear,
        maximumTaxYear = Some(maximumTaxYear),
        minError = BadRequestError,
        maxError = InvalidTaxYearParameterError
      )

      "given a tax year earlier than the minimum and a non-default MtdError" in {
        val result: Validated[Seq[MtdError], TaxYear] = resolver("2020-21")
        result shouldBe Invalid(List(BadRequestError))
      }

      "given a tax year later than the maximum and a non-default MtdError" in {
        val result: Validated[Seq[MtdError], TaxYear] = resolver("2025-26")
        result shouldBe Invalid(List(InvalidTaxYearParameterError))
      }
    }

    "return InvalidTaxYearParameterError" when {
      "given a valid tax year but below TaxYear.tysTaxYear" in {
        val resolver = ResolveDetailedTaxYear(
          minimumTaxYear = TaxYear.tysTaxYear,
          minError = InvalidTaxYearParameterError
        )

        resolver("2021-22") shouldBe Invalid(List(InvalidTaxYearParameterError))
      }
    }

    "return RuleTaxYearNotEndedError" when {
      "given an incomplete tax year and incomplete years are not allowed" in {
        val currentTaxYear = TaxYear.currentTaxYear
        val resolver = ResolveDetailedTaxYear(
          minimumTaxYear = TaxYear.fromMtd("2021-22"),
          allowIncompleteTaxYear = false
        )

        val result: Validated[Seq[MtdError], TaxYear] = resolver(currentTaxYear.asMtd)
        result shouldBe Invalid(List(RuleTaxYearNotEndedError))
      }
    }

    "return format and range errors" when {
      "given an invalid tax year format" in {
        val resolver = ResolveDetailedTaxYear(
          minimumTaxYear = TaxYear.fromMtd("2021-22")
        )

        resolver("2019") shouldBe Invalid(List(TaxYearFormatError))
      }

      "given a tax year string in which the range is greater than 1 year" in {
        val resolver = ResolveDetailedTaxYear(
          minimumTaxYear = TaxYear.fromMtd("2021-22")
        )

        resolver("2017-19") shouldBe Invalid(List(RuleTaxYearRangeInvalidError))
      }

      "the end year is before the start year" in {
        val resolver = ResolveDetailedTaxYear(
          minimumTaxYear = TaxYear.fromMtd("2021-22")
        )

        resolver("2018-17") shouldBe Invalid(List(RuleTaxYearRangeInvalidError))
      }

      "the start and end years are the same" in {
        val resolver = ResolveDetailedTaxYear(
          minimumTaxYear = TaxYear.fromMtd("2021-22")
        )

        resolver("2017-17") shouldBe Invalid(List(RuleTaxYearRangeInvalidError))
      }

      "the tax year is an incorrect format" in {
        val resolver = ResolveDetailedTaxYear(
          minimumTaxYear = TaxYear.fromMtd("2021-22")
        )

        resolver("20177-17") shouldBe Invalid(List(TaxYearFormatError))
      }
    }
  }

}
