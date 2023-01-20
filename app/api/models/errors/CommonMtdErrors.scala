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

package api.models.errors

import play.api.http.Status._

//scalastyle:off

object MtdErrorWithCustomMessage {

  def unapply(arg: MtdError): Option[String] = Some(arg.code)
}

// Format errors
object NinoFormatError    extends MtdError("FORMAT_NINO", "The provided NINO is invalid", BAD_REQUEST)
object TaxYearFormatError extends MtdError("FORMAT_TAX_YEAR", "The provided tax year is invalid", BAD_REQUEST)
object AmountFormatError  extends MtdError("FORMAT_LOSS_AMOUNT", "The format of the loss amount is invalid", BAD_REQUEST)
object LossIdFormatError  extends MtdError("FORMAT_LOSS_ID", "The provided loss ID is invalid", BAD_REQUEST)
object ClaimIdFormatError extends MtdError("FORMAT_CLAIM_ID", "The provided claim ID is invalid", BAD_REQUEST)

object SelfEmploymentIdFormatError extends MtdError("FORMAT_SELF_EMPLOYMENT_ID", "The supplied self-employment ID format is invalid", BAD_REQUEST)

object TypeOfLossFormatError extends MtdError("FORMAT_TYPE_OF_LOSS", "The supplied type of loss format is invalid", BAD_REQUEST)

object TypeOfClaimFormatError
    extends MtdError("FORMAT_TYPE_OF_CLAIM", "The supplied type of claim format is invalid or the type of claim is not recognised", BAD_REQUEST)

object ClaimTypeFormatError       extends MtdError("FORMAT_CLAIM_TYPE", "The provided claim type is invalid", BAD_REQUEST)
object SequenceFormatError        extends MtdError("FORMAT_SEQUENCE", "The provided sequence number is invalid", BAD_REQUEST)
object CountryCodeFormatError     extends MtdError("FORMAT_COUNTRY_CODE", "The format of the country code is invalid", BAD_REQUEST)
object ProviderNameFormatError    extends MtdError("FORMAT_PROVIDER_NAME", "The provided name is invalid", BAD_REQUEST)
object ProviderAddressFormatError extends MtdError("FORMAT_PROVIDERS_ADDRESS", "The provided address is invalid", BAD_REQUEST)
object QOPSRefFormatError         extends MtdError("FORMAT_QOPS_REF", "The provided QOPS reference number is invalid", BAD_REQUEST)

object PensionSchemeTaxRefFormatError
    extends MtdError("FORMAT_PENSION_SCHEME_TAX_REFERENCE", "The provided pension scheme tax reference is invalid", BAD_REQUEST)

object ValueFormatError extends MtdError("FORMAT_VALUE", "The field should be between 0 and 99999999999.99", BAD_REQUEST)

// Rule Errors
object RuleTaxYearNotSupportedError
    extends MtdError("RULE_TAX_YEAR_NOT_SUPPORTED", "The tax year specified does not lie within the supported range", BAD_REQUEST)

object RuleIncorrectOrEmptyBodyError
    extends MtdError("RULE_INCORRECT_OR_EMPTY_BODY_SUBMITTED", "An empty or non-matching body was submitted", BAD_REQUEST)

object RuleTaxYearRangeInvalidError
    extends MtdError("RULE_TAX_YEAR_RANGE_INVALID", "Tax year range invalid. A tax year range of one year is required", BAD_REQUEST)

object RuleTaxYearNotEndedError
    extends MtdError("RULE_TAX_YEAR_NOT_ENDED", "The tax year for this brought forward loss has not yet ended", BAD_REQUEST)

object RuleSelfEmploymentId
    extends MtdError("RULE_SELF_EMPLOYMENT_ID", "A self-employment ID can only be supplied for a self-employment business type", BAD_REQUEST)

object RuleInvalidLossAmount
    extends MtdError("RULE_LOSS_AMOUNT", "Amount should be a positive number less than 99999999999.99 with up to 2 decimal places", BAD_REQUEST)

object RuleDuplicateSubmissionError
    extends MtdError("RULE_DUPLICATE_SUBMISSION", "A brought forward loss already exists for this income source", BAD_REQUEST)

object RuleDuplicateClaimSubmissionError extends MtdError("RULE_DUPLICATE_SUBMISSION", "This claim matches a previous submission", BAD_REQUEST)

object RuleDeleteAfterCrystallisationError
    extends MtdError("RULE_DELETE_AFTER_CRYSTALLISATION", "This loss cannot be deleted after crystallisation", BAD_REQUEST)

object RuleTypeOfClaimInvalid
    extends MtdError("RULE_TYPE_OF_CLAIM_INVALID", "The claim type selected is not available for this type of loss", BAD_REQUEST)

object RuleClaimTypeNotChanged
    extends MtdError("RULE_ALREADY_EXISTS", "The type of claim has already been requested in this tax year for this income source", BAD_REQUEST)

object RulePeriodNotEnded       extends MtdError("RULE_PERIOD_NOT_ENDED", "The relevant accounting period has not yet ended", BAD_REQUEST)
object RuleLossAmountNotChanged extends MtdError("RULE_NO_CHANGE", "The brought forward loss amount has not changed", BAD_REQUEST)

object RuleNoAccountingPeriod extends MtdError("RULE_NO_ACCOUNTING_PERIOD", "For the year of the claim there is no accounting period", BAD_REQUEST)

object RuleInvalidSequenceStart extends MtdError("RULE_INVALID_SEQUENCE_START", "The sequence does not begin with 1", BAD_REQUEST)
object RuleSequenceOrderBroken  extends MtdError("RULE_SEQUENCE_ORDER_BROKEN", "The sequence is not continuous", BAD_REQUEST)

object RuleLossClaimsMissing extends MtdError("RULE_LOSS_CLAIMS_MISSING", "One or more loss claims missing from this request", BAD_REQUEST)

object RuleCountryCodeError extends MtdError("RULE_COUNTRY_CODE", "The country code is not a valid ISO 3166-1 alpha-3 country code", BAD_REQUEST)

object RuleIsAnnualAllowanceReducedError
    extends MtdError("RULE_IS_ANNUAL_ALLOWANCE_REDUCED", "Tapered annual allowance or money purchased allowance has not been provided", BAD_REQUEST)

object RuleBenefitExcessesError
    extends MtdError(
      "RULE_BENEFIT",
      "You can only provide Lump sum benefit taken in excess of lifetime allowance or Benefit in excess of lifetime allowance not both",
      BAD_REQUEST)

object RulePensionReferenceError
    extends MtdError(
      "RULE_PENSION_REFERENCE",
      "You can only provide qualifying recognised Overseas Pension Scheme reference or pension scheme tax reference not both",
      BAD_REQUEST)

//Standard Errors
object NotFoundError           extends MtdError("MATCHING_RESOURCE_NOT_FOUND", "Matching resource not found", NOT_FOUND)
object StandardDownstreamError extends MtdError("INTERNAL_SERVER_ERROR", "An internal server error occurred", INTERNAL_SERVER_ERROR)
object BadRequestError         extends MtdError("INVALID_REQUEST", "Invalid request", BAD_REQUEST)
object BVRError                extends MtdError("BUSINESS_ERROR", "Business validation error", BAD_REQUEST)
object ServiceUnavailableError extends MtdError("SERVICE_UNAVAILABLE", "Internal server error", INTERNAL_SERVER_ERROR)

object InvalidBodyTypeError extends MtdError("INVALID_BODY_TYPE", "Expecting text/json or application/json body", UNSUPPORTED_MEDIA_TYPE)

//Authorisation Errors
object UnauthorisedError       extends MtdError("CLIENT_OR_AGENT_NOT_AUTHORISED", "The client and/or agent is not authorised", UNAUTHORIZED)
object InvalidBearerTokenError extends MtdError("UNAUTHORIZED", "Bearer token is missing or not authorized", UNAUTHORIZED)

// Accept header Errors
object InvalidAcceptHeaderError extends MtdError("ACCEPT_HEADER_INVALID", "The accept header is missing or invalid", NOT_ACCEPTABLE)
object UnsupportedVersionError  extends MtdError("NOT_FOUND", "The requested resource could not be found", NOT_FOUND)
