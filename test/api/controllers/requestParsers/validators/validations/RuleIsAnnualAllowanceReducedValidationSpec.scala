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

package api.controllers.requestParsers.validators.validations

import api.models.errors.RuleIsAnnualAllowanceReducedError
import support.UnitSpec

class RuleIsAnnualAllowanceReducedValidationSpec extends UnitSpec {

  val validScenarios = List(
    (true, Some(true), Some(true)),
    (true, Some(true), Some(false)),
    (true, Some(true), None),
    (true, Some(false), Some(true)),
    (true, None, Some(true)),
    (false, None, None),
    (false, Some(false), Some(false)),
    (false, Some(true), Some(true))
  )

  val invalidScenarios = List(
    (true, Some(false), Some(false)),
    (true, Some(false), None),
    (true, None, Some(false)),
    (true, None, None)
  )

  "RuleIsAnnualAllowanceReducedValidation" when {
    validScenarios.foreach { case (isAnnualAllowanceReduced, taperedAnnualAllowance, moneyPurchasedAllowance) =>
      s"return no errors for validate($isAnnualAllowanceReduced, $taperedAnnualAllowance, $moneyPurchasedAllowance)" in {
        RuleIsAnnualAllowanceReducedValidation.validate(isAnnualAllowanceReduced, taperedAnnualAllowance, moneyPurchasedAllowance) shouldBe Nil
      }
    }

    invalidScenarios.foreach { case (isAnnualAllowanceReduced, taperedAnnualAllowance, moneyPurchasedAllowance) =>
      s"return validation error for validate($isAnnualAllowanceReduced, $taperedAnnualAllowance, $moneyPurchasedAllowance)" in {
        val result = RuleIsAnnualAllowanceReducedValidation.validate(isAnnualAllowanceReduced, taperedAnnualAllowance, moneyPurchasedAllowance)

        result shouldBe List(RuleIsAnnualAllowanceReducedError)
      }
    }
  }

}
