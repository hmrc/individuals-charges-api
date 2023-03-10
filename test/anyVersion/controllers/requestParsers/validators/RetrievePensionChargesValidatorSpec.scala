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

package anyVersion.controllers.requestParsers.validators

import anyVersion.models.request.retrievePensionCharges.RetrievePensionChargesRawData
import api.models.errors.{NinoFormatError, RuleTaxYearNotSupportedError, RuleTaxYearRangeInvalidError, TaxYearFormatError}
import mocks.MockAppConfig
import support.UnitSpec

class RetrievePensionChargesValidatorSpec extends UnitSpec with MockAppConfig {
  private val validNino    = "AA123456A"
  private val validTaxYear = "2021-22"

  class Test extends MockAppConfig {
    val validator = new RetrievePensionChargesValidator(mockAppConfig)
    MockAppConfig.minTaxYearPensionCharge returns "2022"
  }

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in new Test {
        validator.validate(RetrievePensionChargesRawData(validNino, validTaxYear)) shouldBe Nil
      }
    }

    // invalid nino
    "return nino format error" when {
      "an invalid nino is supplied" in new Test {
        validator.validate(RetrievePensionChargesRawData("invalidNino", validTaxYear)) shouldBe
          List(NinoFormatError)
      }
    }

    // invalid tax year range
    "return a tax year format error" when {
      "an invalid tax year range is supplied" in new Test {
        validator.validate(RetrievePensionChargesRawData(validNino, "2020-22")) shouldBe
          List(RuleTaxYearRangeInvalidError)
      }
    }

    // invalid tax year - before minimum tax year value
    "return a tax year format error" when {
      "an invalid tax year, before the minimum, is supplied" in new Test {
        validator.validate(RetrievePensionChargesRawData(validNino, "2018-19")) shouldBe
          List(RuleTaxYearNotSupportedError)
      }
    }

    // multiple errors
    "return multiple errors" when {
      "request supplied has multiple errors" in new Test {
        validator.validate(RetrievePensionChargesRawData("badNino", "2019-03-15")) shouldBe
          List(NinoFormatError, TaxYearFormatError)
      }
    }
  }

}