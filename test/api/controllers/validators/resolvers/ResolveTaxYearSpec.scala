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

package api.controllers.validators.resolvers

import api.models.domain.TaxYear
import api.models.errors.{InvalidTaxYearParameterError, MtdError, RuleTaxYearRangeInvalid, TaxYearFormatError}
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import support.UnitSpec

class ResolveTaxYearSpec extends UnitSpec {

  "ResolveTaxYear" should {
    "return no errors" when {
      "passed a valid tax year" in {
        val validTaxYear = "2018-19"
        val result       = ResolveTaxYear(validTaxYear)
        result shouldBe Valid(TaxYear.fromMtd(validTaxYear))
      }
    }

    "return an error" when {
      "passed an invalid tax year format" in {
        val result = ResolveTaxYear("2019")
        result shouldBe Invalid(List(TaxYearFormatError))
      }

      "passed a tax year string in which the range is greater than 1 year" in {
        val result = ResolveTaxYear("2017-19")
        result shouldBe Invalid(List(RuleTaxYearRangeInvalid))
      }

      "the end year is before the start year" in {
        val result = ResolveTaxYear("2018-17")
        result shouldBe Invalid(List(RuleTaxYearRangeInvalid))
      }

      "the start and end years are the same" in {
        val result = ResolveTaxYear("2017-17")
        result shouldBe Invalid(List(RuleTaxYearRangeInvalid))
      }

      "the tax year is bad" in {
        val result = ResolveTaxYear("20177-17")
        result shouldBe Invalid(List(TaxYearFormatError))
      }
    }
  }

  "ResolveTysTaxYear" should {
    "return no errors" when {

      val validTaxYear = "2023-24"

      "given a valid tax year that's above or equal to TaxYear.tysTaxYear" in {
        ResolveTysTaxYear(validTaxYear) shouldBe Valid(TaxYear.fromMtd(validTaxYear))
      }

      "given a valid tax year in an Option" in {
        val result: Validated[Seq[MtdError], Option[TaxYear]] = ResolveTysTaxYear(Option(validTaxYear))
        result shouldBe Valid(Some(TaxYear.fromMtd(validTaxYear)))
      }

      "given an empty Option" in {
        val result: Validated[Seq[MtdError], Option[TaxYear]] = ResolveTysTaxYear(None)
        result shouldBe Valid(None)
      }

    }

    "return an error" when {
      "given a valid tax year but below TaxYear.tysTaxYear" in {
        ResolveTysTaxYear("2021-22") shouldBe Invalid(List(InvalidTaxYearParameterError))
      }
    }
  }

}
