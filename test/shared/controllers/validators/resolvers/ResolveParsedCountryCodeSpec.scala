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

package shared.controllers.validators.resolvers

import cats.data.Validated.{Invalid, Valid}
import shared.models.errors.{CountryCodeFormatError, RuleCountryCodeError}
import shared.utils.UnitSpec

class ResolveParsedCountryCodeSpec extends UnitSpec {

  private val permittedCountryCodes = ResolveParsedCountryCode.permittedCodes.toSeq.sorted

  "ResolveParsedCountryCode" must {

    permittedCountryCodes.foreach { code =>
      s"return valid for permitted country code $code" in {
        val result = ResolveParsedCountryCode(code, "path")
        result shouldBe Valid(code)
      }
    }

    "return valid for an empty optional country code" in {
      val result = ResolveParsedCountryCode(None, "path")
      result shouldBe Valid(None)
    }

    "return valid for a valid optional country code" in {
      val result = ResolveParsedCountryCode(Some("VEN"), "path")
      result shouldBe Valid(Some("VEN"))
    }

    "return a CountryCodeFormatError for a badly formatted country code" in {
      val result = ResolveParsedCountryCode("FRANCE", "path")
      result shouldBe Invalid(List(CountryCodeFormatError.withPath("path")))
    }

    "return a CountryCodeFormatError for a badly formatted optional country code" in {
      val result = ResolveParsedCountryCode(Some("FRANCE"), "path")
      result shouldBe Invalid(List(CountryCodeFormatError.withPath("path")))
    }

    "return a RuleCountryCodeError for an unpermitted ISO 3166-1 alpha-3 country code" in {
      val result = ResolveParsedCountryCode("GBR", "path")
      result shouldBe Invalid(List(RuleCountryCodeError.withPath("path")))
    }

    "return a RuleCountryCodeError for an unpermitted ISO 3166-1 alpha-3 optional country code" in {
      val result = ResolveParsedCountryCode(Some("GBR"), "path")
      result shouldBe Invalid(List(RuleCountryCodeError.withPath("path")))
    }
  }

}
