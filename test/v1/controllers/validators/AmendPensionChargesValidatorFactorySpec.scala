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

package v1.controllers.validators

import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import mocks.MockAppConfig
import play.api.libs.json.JsValue
import support.UnitSpec
import v1.data.AmendPensionChargesData._
import v1.models.request.AmendPensionCharges.{AmendPensionChargesRequestData, PensionCharges}

class AmendPensionChargesValidatorFactorySpec extends UnitSpec with MockAppConfig {
  private implicit val correlationId: String = "1234"

  private val validNino    = "AA123456A"
  private val validTaxYear = "2021-22"

  private val parsedNino    = Nino(validNino)
  private val parsedTaxYear = TaxYear.fromMtd(validTaxYear)

  private val parsedValidFullRequestBody    = fullValidJson.as[PensionCharges]
  private val parsedValidUpdatedRequestBody = fullValidJsonUpdated.as[PensionCharges]

  val validatorFactory                                                = new AmendPensionChargesValidatorFactory(mockAppConfig)
  private def validator(nino: String, taxYear: String, body: JsValue) = validatorFactory.validator(nino, taxYear, body)

  MockAppConfig.minTaxYearPensionCharge.returns("2022")

  "validator" should {
    "return the parsed domain object" when {
      "a valid request is supplied" in {
        val result: Either[ErrorWrapper, AmendPensionChargesRequestData] =
          validator(validNino, validTaxYear, fullValidJson).validateAndWrapResult()

        result shouldBe Right(
          AmendPensionChargesRequestData(parsedNino, parsedTaxYear, parsedValidFullRequestBody)
        )
      }

      "a valid request is supplied with pensionContributions updated" in {
        val result: Either[ErrorWrapper, AmendPensionChargesRequestData] =
          validator(validNino, validTaxYear, fullValidJsonUpdated).validateAndWrapResult()

        result shouldBe Right(
          AmendPensionChargesRequestData(parsedNino, parsedTaxYear, parsedValidUpdatedRequestBody)
        )
      }
    }

    "return path parameter error(s)" when {
      "an invalid nino is supplied" in {
        val result: Either[ErrorWrapper, AmendPensionChargesRequestData] =
          validator("badNino", validTaxYear, fullJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, NinoFormatError)
        )
      }
      "an invalid tax year is supplied" in {
        val result: Either[ErrorWrapper, AmendPensionChargesRequestData] =
          validator(validNino, "2000", fullJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, TaxYearFormatError)
        )
      }
      "the taxYear range is invalid" in {
        val result: Either[ErrorWrapper, AmendPensionChargesRequestData] =
          validator(validNino, "2021-24", fullJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError)
        )
      }
      "an invalid tax year, before the minimum, is supplied" in {
        val result: Either[ErrorWrapper, AmendPensionChargesRequestData] =
          validator(validNino, "2020-21", fullJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleTaxYearNotSupportedError)
        )
      }
      "all path parameters are invalid" in {
        val result: Either[ErrorWrapper, AmendPensionChargesRequestData] =
          validator("badNino", "2000", fullJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, BadRequestError, Some(List(NinoFormatError, TaxYearFormatError)))
        )
      }
    }

    "return a RULE_INCORRECT_OR_EMPTY_BODY_SUBMITTED error" when {
      "an empty request body is supplied" in {
        val result: Either[ErrorWrapper, AmendPensionChargesRequestData] =
          validator(validNino, validTaxYear, emptyJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError)
        )
      }
      "isAnnualAllowanceReduced is missing" in {
        val result: Either[ErrorWrapper, AmendPensionChargesRequestData] =
          validator(validNino, validTaxYear, missingIsAnnualAllowanceReducedJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError)
        )
      }
    }

    "return country errors" when {
      "multiple country codes are invalid for multiple reasons" in {
        val result: Either[ErrorWrapper, AmendPensionChargesRequestData] =
          validator(validNino, validTaxYear, fullJsonWithInvalidCountries).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(List(
              RuleCountryCodeError.copy(paths = Some(
                List(
                  "/pensionSchemeOverseasTransfers/overseasSchemeProvider/1/providerCountryCode",
                  "/overseasPensionContributions/overseasSchemeProvider/0/providerCountryCode"
                )
              )),
              CountryCodeFormatError.copy(paths = Some(
                List(
                  "/pensionSchemeOverseasTransfers/overseasSchemeProvider/0/providerCountryCode",
                  "/overseasPensionContributions/overseasSchemeProvider/1/providerCountryCode"
                )
              ))
            ))
          )
        )
      }
      "multiple country codes are invalid format" in {
        val result: Either[ErrorWrapper, AmendPensionChargesRequestData] =
          validator(validNino, validTaxYear, fullJsonWithInvalidCountryFormat("INVALID")).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            CountryCodeFormatError.copy(paths = Some(
              List(
                "/pensionSchemeOverseasTransfers/overseasSchemeProvider/2/providerCountryCode",
                "/overseasPensionContributions/overseasSchemeProvider/2/providerCountryCode"
              )
            ))
          )
        )
      }
      "multiple country codes are invalid rule" in {
        val result: Either[ErrorWrapper, AmendPensionChargesRequestData] =
          validator(validNino, validTaxYear, fullJsonWithInvalidCountryFormat("BOB")).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            RuleCountryCodeError.copy(paths = Some(
              List(
                "/pensionSchemeOverseasTransfers/overseasSchemeProvider/2/providerCountryCode",
                "/overseasPensionContributions/overseasSchemeProvider/2/providerCountryCode"
              )
            ))
          )
        )
      }
    }

    "return big decimal error" when {
      "an too large number is supplied" in {
        val result: Either[ErrorWrapper, AmendPensionChargesRequestData] =
          validator(validNino, validTaxYear, fullJson(999999999999.99)).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.copy(paths = Some(
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
            ))
          )
        )
      }
      "an too small number is supplied" in {
        val result: Either[ErrorWrapper, AmendPensionChargesRequestData] =
          validator(validNino, validTaxYear, fullJson(-69420.00)).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.copy(paths = Some(
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
            ))
          )
        )
      }
    }
  }

}
