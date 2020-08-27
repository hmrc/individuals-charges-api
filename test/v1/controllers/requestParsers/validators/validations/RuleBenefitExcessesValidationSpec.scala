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

package v1.controllers.requestParsers.validators.validations

import support.UnitSpec
import v1.models.des.LifetimeAllowance
import v1.models.errors.RuleBenefitExcessesError

class RuleBenefitExcessesValidationSpec extends UnitSpec {

  val lumpSumBenefitTakenInExcessOfLifetimeAllowance = LifetimeAllowance(123.45, 1.23)
  val benefitInExcessOfLifetimeAllowance = LifetimeAllowance(123.45, 1.23)

  "Rule benefit in excess validation" when {
    "only one excess provided" must {
      "return no errors for lump sum only" in {
        RuleBenefitExcessesValidation.validate(Some(lumpSumBenefitTakenInExcessOfLifetimeAllowance), None)shouldBe NoValidationErrors
      }
      "return no errors for benefit in excess only" in {
        RuleBenefitExcessesValidation.validate(None, Some(benefitInExcessOfLifetimeAllowance))shouldBe NoValidationErrors
      }
    }
    "neither excess is provided" must {
      "return no errors" in {
        RuleBenefitExcessesValidation.validate(None, None)shouldBe NoValidationErrors
      }
    }
    "both excesses are provided" must {
      "return RULE_BENEFIT error" in {
        RuleBenefitExcessesValidation.validate(
          Some(lumpSumBenefitTakenInExcessOfLifetimeAllowance),
          Some(benefitInExcessOfLifetimeAllowance)) shouldBe List(RuleBenefitExcessesError)
      }
    }
  }
}
