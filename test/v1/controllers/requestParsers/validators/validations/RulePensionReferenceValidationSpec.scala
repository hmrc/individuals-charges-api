/*
 * Copyright 2022 HM Revenue & Customs
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

package v1.controllers.requestParsers.validators.validations

import support.UnitSpec
import v1.models.errors.RulePensionReferenceError

class RulePensionReferenceValidationSpec extends UnitSpec {
  val qualifyingRecognisedOverseasPensionSchemeReferenceNumber = Seq("Q123456")
  val pensionSchemeTaxReference = Seq("00123456RA")

  "Rule Pension Reference validation" when {
    "only one provided" must {
      "return no errors for qualifyingRecognisedOverseasPensionSchemeReferenceNumber" in {
        RulePensionReferenceValidation.validate(Some(qualifyingRecognisedOverseasPensionSchemeReferenceNumber), None)shouldBe NoValidationErrors
      }
      "return no errors for pensionSchemeTaxReference" in {
        RulePensionReferenceValidation.validate(None, Some(pensionSchemeTaxReference))shouldBe NoValidationErrors
      }
    }
    "neither is provided" must {
      "return no errors" in {
        RulePensionReferenceValidation.validate(None, None)shouldBe NoValidationErrors
      }
    }
    "both are provided" must {
      "return RULE_PENSION_REFERENCE error" in {
        RulePensionReferenceValidation.validate(
          Some(qualifyingRecognisedOverseasPensionSchemeReferenceNumber),
          Some(pensionSchemeTaxReference)) shouldBe List(RulePensionReferenceError)
      }
    }
  }
}
