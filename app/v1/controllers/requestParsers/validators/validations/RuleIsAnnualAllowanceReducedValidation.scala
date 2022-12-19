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

import v1.models.errors.{MtdError, RuleIsAnnualAllowanceReducedError}

object RuleIsAnnualAllowanceReducedValidation {

  def validate(isAnnualAllowanceReduced: Option[Boolean], taperedAnnualAllowance: Option[Boolean], moneyPurchasedAllowance: Option[Boolean]): List[MtdError] =
    (isAnnualAllowanceReduced, taperedAnnualAllowance, moneyPurchasedAllowance) match {
      case (Some(true), Some(true), Some(false)) => NoValidationErrors
      case (Some(true), Some(false), Some(true)) => NoValidationErrors
      case (Some(true), None, Some(true))        => NoValidationErrors
      case (Some(true), Some(true), None)        => NoValidationErrors
      case (Some(false), _, _)                   => NoValidationErrors
      case _                               => List(RuleIsAnnualAllowanceReducedError)
    }

}
