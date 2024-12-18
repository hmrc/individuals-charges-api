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

package v2.createAmend.def1

import common.errors.{CountryCodeFormatError, RuleCountryCodeError}
import common.models.domain.{Nino, TaxYear}
import common.errors._
import mocks.MockIndividualsChargesConfig
import play.api.libs.json.{JsObject, JsValue}
import support.UnitSpec
import v2.createAmend.def1.fixture.Def1_CreateAmendPensionChargesFixture._
import v2.createAmend.def1.model.Def1_CreateAmendPensionChargesValidator
import v2.createAmend.def1.model.request.{Def1_CreateAmendPensionChargesRequestBody, Def1_CreateAmendPensionChargesRequestData}
import v2.createAmend.model.request.CreateAmendPensionChargesRequestData

class Def1_CreateAmendPensionChargesValidatorSpec extends UnitSpec with MockIndividualsChargesConfig {
  private implicit val correlationId: String = "1234"

  private val validNino    = "AA123456A"
  private val validTaxYear = "2021-22"

  private val parsedNino    = Nino(validNino)
  private val parsedTaxYear = TaxYear.fromMtd(validTaxYear)

  private val parsedFullRequestBody    = fullValidJson.as[Def1_CreateAmendPensionChargesRequestBody]
  private val parsedUpdatedRequestBody = fullValidJsonUpdated.as[Def1_CreateAmendPensionChargesRequestBody]

  private def validator(nino: String, taxYear: String, body: JsValue) = new Def1_CreateAmendPensionChargesValidator(nino, taxYear, body)(mockAppConfig)

  class Test {
    MockedIndividualsChargesConfig.minTaxYearPensionCharge.returns("2022")
  }

  "validator" should {
    "return the parsed domain object" when {
      "a valid request is supplied" in new Test {
        val result: Either[ErrorWrapper, CreateAmendPensionChargesRequestData] =
          validator(validNino, validTaxYear, fullValidJson).validateAndWrapResult()

        result shouldBe Right(
          Def1_CreateAmendPensionChargesRequestData(parsedNino, parsedTaxYear, parsedFullRequestBody)
        )
      }

      "a valid request is supplied with pensionContributions updated" in new Test {
        val result: Either[ErrorWrapper, CreateAmendPensionChargesRequestData] =
          validator(validNino, validTaxYear, fullValidJsonUpdated).validateAndWrapResult()

        result shouldBe Right(
          Def1_CreateAmendPensionChargesRequestData(parsedNino, parsedTaxYear, parsedUpdatedRequestBody)
        )
      }
    }

    "return path parameter error(s)" when {
      "an invalid nino is supplied" in new Test {
        val result: Either[ErrorWrapper, CreateAmendPensionChargesRequestData] =
          validator("badNino", validTaxYear, fullJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, NinoFormatError)
        )
      }
      "an invalid tax year is supplied" in new Test {
        val result: Either[ErrorWrapper, CreateAmendPensionChargesRequestData] =
          validator(validNino, "2000", fullJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, TaxYearFormatError)
        )
      }
      "the taxYear range is invalid" in new Test {
        val result: Either[ErrorWrapper, CreateAmendPensionChargesRequestData] =
          validator(validNino, "2021-24", fullJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError)
        )
      }
      "an invalid tax year, before the minimum, is supplied" in new Test {
        val result: Either[ErrorWrapper, CreateAmendPensionChargesRequestData] =
          validator(validNino, "2020-21", fullJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleTaxYearNotSupportedError)
        )
      }
      "all path parameters are invalid" in new Test {
        val result: Either[ErrorWrapper, CreateAmendPensionChargesRequestData] =
          validator("badNino", "2000", fullJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, BadRequestError, Some(List(NinoFormatError, TaxYearFormatError)))
        )
      }
    }

    "return a RULE_INCORRECT_OR_EMPTY_BODY_SUBMITTED error" when {
      "an empty request body is supplied" in new Test {
        val result: Either[ErrorWrapper, CreateAmendPensionChargesRequestData] =
          validator(validNino, validTaxYear, JsObject.empty).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError)
        )
      }

      "extra field in the request body is supplied" in new Test {
        val result: Either[ErrorWrapper, CreateAmendPensionChargesRequestData] =
          validator(validNino, validTaxYear, invalidJsonWithExtraField).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/pensionSavingsTaxChargesExtra"))
        )
      }
    }

    "return country errors" when {
      "multiple country codes are invalid for multiple reasons" in new Test {
        val result: Either[ErrorWrapper, CreateAmendPensionChargesRequestData] =
          validator(validNino, validTaxYear, fullJsonWithInvalidCountries).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(List(
              RuleCountryCodeError.withPaths(
                List(
                  "/pensionSchemeOverseasTransfers/overseasSchemeProvider/1/providerCountryCode",
                  "/overseasPensionContributions/overseasSchemeProvider/0/providerCountryCode"
                )
              ),
              CountryCodeFormatError.withPaths(
                List(
                  "/pensionSchemeOverseasTransfers/overseasSchemeProvider/0/providerCountryCode",
                  "/overseasPensionContributions/overseasSchemeProvider/1/providerCountryCode"
                )
              )
            ))
          )
        )
      }
      "multiple country codes are invalid format" in new Test {
        val result: Either[ErrorWrapper, CreateAmendPensionChargesRequestData] =
          validator(validNino, validTaxYear, fullJsonWithInvalidCountryFormat("INVALID")).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            CountryCodeFormatError.withPaths(
              List(
                "/pensionSchemeOverseasTransfers/overseasSchemeProvider/2/providerCountryCode",
                "/overseasPensionContributions/overseasSchemeProvider/2/providerCountryCode"
              )
            )
          )
        )
      }
      "multiple country codes are invalid rule" in new Test {
        val result: Either[ErrorWrapper, CreateAmendPensionChargesRequestData] =
          validator(validNino, validTaxYear, fullJsonWithInvalidCountryFormat("BOB")).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            RuleCountryCodeError.withPaths(
              List(
                "/pensionSchemeOverseasTransfers/overseasSchemeProvider/2/providerCountryCode",
                "/overseasPensionContributions/overseasSchemeProvider/2/providerCountryCode"
              )
            )
          )
        )
      }
    }

    "return big decimal error" when {
      "an too large number is supplied" in new Test {
        val result: Either[ErrorWrapper, CreateAmendPensionChargesRequestData] =
          validator(validNino, validTaxYear, fullJson(999999999999.99)).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.withPaths(
              List(
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
              )
            )
          )
        )
      }
      "an too small number is supplied" in new Test {
        val result: Either[ErrorWrapper, CreateAmendPensionChargesRequestData] =
          validator(validNino, validTaxYear, fullJson(-69420.00)).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.withPaths(
              List(
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
              )
            )
          )
        )
      }
    }
  }

}
