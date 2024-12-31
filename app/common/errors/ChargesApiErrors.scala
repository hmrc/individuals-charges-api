/*
 * Copyright 2024 HM Revenue & Customs
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

package common.errors

import play.api.http.Status._
import shared.models.errors.MtdError

//scalastyle:off

// Format errors
object ProviderNameFormatError    extends MtdError("FORMAT_PROVIDER_NAME", "The provided name is invalid", BAD_REQUEST)
object ProviderAddressFormatError extends MtdError("FORMAT_PROVIDERS_ADDRESS", "The provided address is invalid", BAD_REQUEST)
object QOPSRefFormatError         extends MtdError("FORMAT_QOPS_REF", "The provided QOPS reference number is invalid", BAD_REQUEST)

object PensionSchemeTaxRefFormatError
    extends MtdError("FORMAT_PENSION_SCHEME_TAX_REFERENCE", "The provided pension scheme tax reference is invalid", BAD_REQUEST)

// Rule errors

object RuleIsAnnualAllowanceReducedError
    extends MtdError("RULE_IS_ANNUAL_ALLOWANCE_REDUCED", "Tapered annual allowance or money purchased allowance has not been provided", BAD_REQUEST)

object RulePensionReferenceError
    extends MtdError(
      "RULE_PENSION_REFERENCE",
      "You can only provide qualifying recognised Overseas Pension Scheme reference or pension scheme tax reference not both",
      BAD_REQUEST)
