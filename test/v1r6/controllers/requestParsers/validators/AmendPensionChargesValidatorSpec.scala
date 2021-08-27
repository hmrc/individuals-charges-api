/*
 * Copyright 2021 HM Revenue & Customs
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

package v1r6.controllers.requestParsers.validators

import v1r6.data.AmendPensionChargesData._
import mocks.MockAppConfig
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContentAsJson
import support.UnitSpec
import v1r6.models.errors._
import v1r6.models.request.AmendPensionCharges

class AmendPensionChargesValidatorSpec extends UnitSpec with MockAppConfig {

  private val validNino = "AA123456A"
  private val validTaxYear = "2021-22"

  class Test extends MockAppConfig {
    val validator = new AmendPensionChargesValidator(mockAppConfig)
    MockAppConfig.minTaxYearPensionCharge returns "2022"

    val emptyRequestBodyJson: JsValue = Json.parse("""{}""")

  }

  "Running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in new Test {
        validator.validate(AmendPensionCharges.AmendPensionChargesRawData(validNino, validTaxYear, AnyContentAsJson(fullValidJson))) shouldBe Nil
      }
    }

    "return country errors" when {
      "multiple country codes are invalid for multiple reasons" in new Test {
        validator.validate(AmendPensionCharges.AmendPensionChargesRawData(validNino, validTaxYear, AnyContentAsJson(fullJsonWithInvalidCountries))) shouldBe List(
          RuleCountryCodeError.copy(paths = Some(
            Seq(
              "/pensionSchemeOverseasTransfers/overseasSchemeProvider/1/providerCountryCode",
              "/overseasPensionContributions/overseasSchemeProvider/0/providerCountryCode"
            )
          )),
          CountryCodeFormatError.copy(paths = Some(
            Seq(
              "/pensionSchemeOverseasTransfers/overseasSchemeProvider/0/providerCountryCode",
              "/overseasPensionContributions/overseasSchemeProvider/1/providerCountryCode"
            )
          ))
        )
      }
      "multiple country codes are invalid format" in new Test {
        validator.validate(AmendPensionCharges.AmendPensionChargesRawData(validNino, validTaxYear, AnyContentAsJson(fullJsonWithInvalidCountryFormat("INVALID")))) shouldBe List(
          CountryCodeFormatError.copy(paths = Some(
            Seq(
              "/pensionSchemeOverseasTransfers/overseasSchemeProvider/2/providerCountryCode",
              "/overseasPensionContributions/overseasSchemeProvider/2/providerCountryCode"
            )
          ))
        )
      }
      "multiple country codes are invalid rule" in new Test {
        validator.validate(AmendPensionCharges.AmendPensionChargesRawData(validNino, validTaxYear, AnyContentAsJson(fullJsonWithInvalidCountryFormat("BOB")))) shouldBe List(
          RuleCountryCodeError.copy(paths = Some(
            Seq(
              "/pensionSchemeOverseasTransfers/overseasSchemeProvider/2/providerCountryCode",
              "/overseasPensionContributions/overseasSchemeProvider/2/providerCountryCode"
            )
          ))
        )
      }
    }

    "return big decimal error" when {
      "an too large number is supplied" in new Test {
        validator.validate(AmendPensionCharges.AmendPensionChargesRawData(validNino, validTaxYear, AnyContentAsJson(fullJson(999999999999.99)))) shouldBe List(
          MtdError("FORMAT_VALUE", "The field should be between 0 and 99999999999.99",
            Some(List(
              "/pensionSavingsTaxCharges/lumpSumBenefitTakenInExcessOfLifetimeAllowance/amount",
              "/pensionSavingsTaxCharges/lumpSumBenefitTakenInExcessOfLifetimeAllowance/taxPaid",
              "/pensionSchemeOverseasTransfers/transferChargeTaxPaid",
              "/pensionSchemeOverseasTransfers/transferCharge",
              "/pensionSchemeUnauthorisedPayments/surcharge/amount",
              "/pensionSchemeUnauthorisedPayments/surcharge/foreignTaxPaid",
              "/pensionSchemeUnauthorisedPayments/noSurcharge/amount",
              "/pensionSchemeUnauthorisedPayments/noSurcharge/foreignTaxPaid",
              "/pensionContributions/annualAllowanceTaxPaid",
              "/pensionContributions/inExcessOfTheAnnualAllowance",
              "/overseasPensionContributions/shortServiceRefund",
              "/overseasPensionContributions/shortServiceRefundTaxPaid"
            )))
        )
      }
      "an too small number is supplied" in new Test {
        validator.validate(AmendPensionCharges.AmendPensionChargesRawData(validNino, validTaxYear, AnyContentAsJson(fullJson(-69420.00)))) shouldBe List(
          MtdError("FORMAT_VALUE", "The field should be between 0 and 99999999999.99",
            Some(List(
              "/pensionSavingsTaxCharges/lumpSumBenefitTakenInExcessOfLifetimeAllowance/amount",
              "/pensionSavingsTaxCharges/lumpSumBenefitTakenInExcessOfLifetimeAllowance/taxPaid",
              "/pensionSchemeOverseasTransfers/transferChargeTaxPaid",
              "/pensionSchemeOverseasTransfers/transferCharge",
              "/pensionSchemeUnauthorisedPayments/surcharge/amount",
              "/pensionSchemeUnauthorisedPayments/surcharge/foreignTaxPaid",
              "/pensionSchemeUnauthorisedPayments/noSurcharge/amount",
              "/pensionSchemeUnauthorisedPayments/noSurcharge/foreignTaxPaid",
              "/pensionContributions/annualAllowanceTaxPaid",
              "/pensionContributions/inExcessOfTheAnnualAllowance",
              "/overseasPensionContributions/shortServiceRefund",
              "/overseasPensionContributions/shortServiceRefundTaxPaid"
            )))
        )
      }
    }

    "return a path parameter error" when {
      "an invalid nino is supplied" in new Test {
        validator.validate(AmendPensionCharges.AmendPensionChargesRawData("badNino", validTaxYear, AnyContentAsJson(fullJson))) shouldBe List(NinoFormatError)
      }
      "an invalid tax year is supplied" in new Test {
        validator.validate(AmendPensionCharges.AmendPensionChargesRawData(validNino, "2000", AnyContentAsJson(fullJson))) shouldBe List(TaxYearFormatError)
      }
      "the taxYear range is invalid" in new Test {
        validator.validate(AmendPensionCharges.AmendPensionChargesRawData(validNino, "2021-24", AnyContentAsJson(fullJson))) shouldBe List(RuleTaxYearRangeInvalid)
      }
      "all path parameters are invalid" in new Test {
        validator.validate(AmendPensionCharges.AmendPensionChargesRawData("badNino", "2000", AnyContentAsJson(fullJson))) shouldBe List(NinoFormatError, TaxYearFormatError)
      }
      "return a RULE_INCORRECT_OR_EMPTY_BODY_SUBMITTED error" when {
        "an empty body" in new Test {
          validator.validate(AmendPensionCharges.AmendPensionChargesRawData(validNino, validTaxYear, body = AnyContentAsJson(emptyJson))) shouldBe List(
            RuleIncorrectOrEmptyBodyError)
        }
      }
      "not return a RULE_INCORRECT_OR_EMPTY_BODY_SUBMITTED error" when {
        "only one field is supplied in body" in new Test {
          validator.validate(AmendPensionCharges.AmendPensionChargesRawData(validNino, validTaxYear, body = AnyContentAsJson(minimalJson))) shouldBe List()
        }
      }
    }
  }
}
