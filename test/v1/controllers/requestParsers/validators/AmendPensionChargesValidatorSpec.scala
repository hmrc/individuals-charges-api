/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.controllers.requestParsers.validators

import data.AmendPensionChargesData.{emptyJson, fullJson}
import mocks.MockAppConfig
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContentAsJson
import support.UnitSpec
import v1.models.errors.{NinoFormatError, RuleIncorrectOrEmptyBodyError, RuleTaxYearNotEndedError, RuleTaxYearRangeInvalid, TaxYearFormatError}
import v1.models.requestData.AmendPensionChargesRawData

class AmendPensionChargesValidatorSpec extends UnitSpec with MockAppConfig {

  private val validNino = "AA123456A"
  private val validTaxYear = "2021-22"

  class Test extends MockAppConfig {
    val validator = new AmendPensionChargesValidator(mockAppConfig)
    MockedAppConfig.minTaxYearPensionCharge returns "2022"

     val emptyRequestBodyJson: JsValue = Json.parse("""{}""")

  }

  "Running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in new Test {
        validator.validate(AmendPensionChargesRawData(validNino,validTaxYear,AnyContentAsJson(fullJson))) shouldBe Nil
      }
    }
    "return a path parameter error" when {
      "an invalid nino is supplied" in new Test {
        validator.validate(AmendPensionChargesRawData("badNino",validTaxYear,AnyContentAsJson(fullJson))) shouldBe List(NinoFormatError)
      }
      "an invalid tax year is supplied" in new Test {
        validator.validate(AmendPensionChargesRawData(validNino,"2000", AnyContentAsJson(fullJson))) shouldBe List(TaxYearFormatError)
      }
      "the taxYear range is invalid" in new Test {
        validator.validate(AmendPensionChargesRawData(validNino,"2021-24", AnyContentAsJson(fullJson))) shouldBe List(RuleTaxYearRangeInvalid)
      }
      "all path parameters are invalid" in new Test {
        validator.validate(AmendPensionChargesRawData("badNino","2000", AnyContentAsJson(fullJson))) shouldBe List(NinoFormatError,TaxYearFormatError)
      }
      "return a RULE_INCORRECT_OR_EMPTY_BODY_SUBMITTED error" when {
        "an empty body" in new Test {
          validator.validate(AmendPensionChargesRawData(validNino, validTaxYear, body = AnyContentAsJson(emptyJson))) shouldBe List(
            RuleIncorrectOrEmptyBodyError)
        }
      }
    }
  }





}
