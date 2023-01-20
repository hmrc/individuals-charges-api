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

package v1.controllers.requestParsers.validators.validations

import api.models.errors.RuleIsAnnualAllowanceReducedError
import support.UnitSpec

class RuleAnnualAllowanceReducedValidationSpec extends UnitSpec {

  "Rule annual allowance" when {
    "only is annual allowance reduced provided" must {
      "return a validation error when only isOnlyAnnualAllowanceReduced" in {
        RuleIsAnnualAllowanceReducedValidation.
          validate(isAnnualAllowanceReduced = Some(true), Some(false), Some(false)) shouldBe List(RuleIsAnnualAllowanceReducedError)
      }
    }
    "two are provided there should be no errors" must {
      "return no errors with isAnnualAllowanceReduced and moeyPurchasedAllowance" in {
        RuleIsAnnualAllowanceReducedValidation.
          validate(isAnnualAllowanceReduced = Some(true), Some(false), Some(true)) shouldBe Nil
      }
      "return no errors with is annualAllowanceReduced and moneyPurchasedAllowance" in {
        RuleIsAnnualAllowanceReducedValidation.
          validate(isAnnualAllowanceReduced = Some(true), Some(true), Some(false)) shouldBe Nil
      }
        "return no errors when all booleans are false" in {
        RuleIsAnnualAllowanceReducedValidation.validate(None, None, None) shouldBe Nil
      }
    }
  }

}
