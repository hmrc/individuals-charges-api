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

package v2.createAmend.def2.model

import api.models.errors._
import shared.controllers.validators.resolvers._
import shared.controllers.validators.{RulesValidator, Validator}
import shared.models.errors._
import cats.data.Validated
import cats.data.Validated.Invalid
import cats.implicits._
import config.IndividualsChargesConfig
import play.api.libs.json.JsValue
import shared.models.domain.TaxYear
import v2.createAmend.def2.model.Def2_CreateAmendPensionChargesRulesValidator.validateBusinessRules
import v2.createAmend.def2.model.request.{Def2_CreateAmendPensionChargesRequestBody, Def2_CreateAmendPensionChargesRequestData}
import v2.createAmend.model.request.CreateAmendPensionChargesRequestData

import javax.inject.Inject

class Def2_CreateAmendPensionChargesValidator @Inject() (nino: String, taxYear: String, body: JsValue)(appConfig: IndividualsChargesConfig)
    extends Validator[CreateAmendPensionChargesRequestData] {

  private lazy val minTaxYear = TaxYear(appConfig.minTaxYearPensionCharge)
  private val resolveJson     = ResolveJsonObject.strictResolver[Def2_CreateAmendPensionChargesRequestBody]

  def validate: Validated[Seq[MtdError], CreateAmendPensionChargesRequestData] =
    (
      ResolveNino(nino),
      ResolveTaxYear(minTaxYear, taxYear),
      resolveJson(body)
    ).mapN(Def2_CreateAmendPensionChargesRequestData) andThen validateBusinessRules

}

object Def2_CreateAmendPensionChargesRulesValidator extends RulesValidator[Def2_CreateAmendPensionChargesRequestData] {
  private val resolveParsedNumber      = ResolveParsedNumber()
  private val qropsRefRegex            = "^[Q]{1}[0-9]{6}$".r
  private val pensionSchemeTaxRefRegex = "^\\d{8}[R]{1}[a-zA-Z]{1}$".r

  def validateBusinessRules(
      parsed: Def2_CreateAmendPensionChargesRequestData): Validated[Seq[MtdError], Def2_CreateAmendPensionChargesRequestData] = {

    import parsed._

    combine(
      validateRulePensionReference(body),
      validateNames(body),
      validateAddresses(body),
      validateQROPsReferences(body),
      validatePensionSchemeTaxReference(body),
      validateRuleIsAnnualAllowanceReduced(body.pensionContributions),
      validateCharges(body),
      validateCountryCodes(body)
    ).onSuccess(parsed)
  }

  private def validateRulePensionReference(pensionCharges: request.Def2_CreateAmendPensionChargesRequestBody): Validated[Seq[MtdError], Unit] = {
    import pensionCharges._

    def validatePensionReference(overseasSchemeProviders: Seq[request.OverseasSchemeProvider]): Validated[Seq[MtdError], Unit] =
      overseasSchemeProviders.traverse_(schemeProvider => {
        import schemeProvider._
        (qualifyingRecognisedOverseasPensionScheme, pensionSchemeTaxReference) match {
          case (Some(_), Some(_)) => Invalid(List(RulePensionReferenceError))
          case _                  => valid
        }
      })

    combine(
      pensionSchemeOverseasTransfers
        .traverse(pensionSchemeOverseasTransfers => validatePensionReference(pensionSchemeOverseasTransfers.overseasSchemeProvider)),
      overseasPensionContributions
        .traverse(overseasPensionContributions => validatePensionReference(overseasPensionContributions.overseasSchemeProvider))
    )
  }

  private def validateNames(pensionCharges: request.Def2_CreateAmendPensionChargesRequestBody): Validated[Seq[MtdError], Unit] = {
    import pensionCharges._

    def validateProviderName(startOfPath: String, overseasSchemeProviders: Seq[request.OverseasSchemeProvider]): Validated[Seq[MtdError], Unit] = {
      overseasSchemeProviders.zipWithIndex.traverse_ { case (schemeProviderWithIndex, index) =>
        import schemeProviderWithIndex._
        val nameMaxLength = 105
        if (providerName.length() <= nameMaxLength && providerName.nonEmpty) {
          valid
        } else {
          Invalid(List(ProviderNameFormatError.withPath(s"/$startOfPath/overseasSchemeProvider/$index/providerName")))
        }
      }
    }

    combine(
      pensionSchemeOverseasTransfers
        .traverse(pensionSchemeOverseasTransfers =>
          validateProviderName("pensionSchemeOverseasTransfers", pensionSchemeOverseasTransfers.overseasSchemeProvider)),
      overseasPensionContributions
        .traverse(overseasPensionContributions =>
          validateProviderName("overseasPensionContributions", overseasPensionContributions.overseasSchemeProvider))
    )
  }

  private def validateAddresses(pensionCharges: request.Def2_CreateAmendPensionChargesRequestBody): Validated[Seq[MtdError], Unit] = {
    import pensionCharges._

    def validateProviderAddress(startOfPath: String, overseasSchemeProviders: Seq[request.OverseasSchemeProvider]): Validated[Seq[MtdError], Unit] = {
      overseasSchemeProviders.zipWithIndex.traverse_ { case (schemeProviderWithIndex, index) =>
        import schemeProviderWithIndex._
        val addressMaxLength = 250
        if (providerAddress.length() <= addressMaxLength && providerAddress.nonEmpty) {
          valid
        } else {
          Invalid(List(ProviderAddressFormatError.withPath(s"/$startOfPath/overseasSchemeProvider/$index/providerAddress")))
        }
      }
    }

    combine(
      pensionSchemeOverseasTransfers
        .traverse(pensionSchemeOverseasTransfers =>
          validateProviderAddress("pensionSchemeOverseasTransfers", pensionSchemeOverseasTransfers.overseasSchemeProvider)),
      overseasPensionContributions
        .traverse(overseasPensionContributions =>
          validateProviderAddress("overseasPensionContributions", overseasPensionContributions.overseasSchemeProvider))
    )
  }

  private def validateQROPsReferences(pensionCharges: request.Def2_CreateAmendPensionChargesRequestBody): Validated[Seq[MtdError], Unit] = {
    import pensionCharges._

    def validateQropsRef(startOfPath: String, overseasSchemeProviders: Seq[request.OverseasSchemeProvider]): Validated[Seq[MtdError], Unit] = {
      overseasSchemeProviders.zipWithIndex.traverse_ { case (schemeProviderWithIndex, index) =>
        schemeProviderWithIndex.qualifyingRecognisedOverseasPensionScheme
          .traverse { qualifyingRecognisedOverseasPensionScheme =>
            qualifyingRecognisedOverseasPensionScheme.zipWithIndex.traverse_ { case (qropsReference, qropsIndex) =>
              if (qropsRefRegex.matches(qropsReference)) {
                valid
              } else {
                Invalid(List(
                  QOPSRefFormatError.withPath(s"/$startOfPath/overseasSchemeProvider/$index/qualifyingRecognisedOverseasPensionScheme/$qropsIndex")))
              }
            }
          }
      }
    }

    combine(
      pensionSchemeOverseasTransfers
        .traverse(pensionSchemeOverseasTransfers =>
          validateQropsRef("pensionSchemeOverseasTransfers", pensionSchemeOverseasTransfers.overseasSchemeProvider)),
      overseasPensionContributions
        .traverse(overseasPensionContributions =>
          validateQropsRef("overseasPensionContributions", overseasPensionContributions.overseasSchemeProvider))
    )
  }

  private def validatePensionSchemeTaxReference(pensionCharges: request.Def2_CreateAmendPensionChargesRequestBody): Validated[Seq[MtdError], Unit] = {
    import pensionCharges._

    def validatePensionSchemeTaxRef(startOfPath: String,
                                    overseasSchemeProviders: Seq[request.OverseasSchemeProvider]): Validated[Seq[MtdError], Unit] = {
      overseasSchemeProviders.zipWithIndex.traverse_ { case (schemeProviderWithIndex, index) =>
        import schemeProviderWithIndex._
        pensionSchemeTaxReference
          .traverse { references =>
            validateReferences(s"$startOfPath/overseasSchemeProvider/$index", references)
          }
      }
    }

    def validateReferences(startOfPath: String, pensionSchemeTaxReference: Seq[String]): Validated[Seq[MtdError], Unit] = {
      pensionSchemeTaxReference.zipWithIndex.traverse_ { case (reference, referenceIndex) =>
        if (pensionSchemeTaxRefRegex.matches(reference)) {
          valid
        } else {
          Invalid(List(PensionSchemeTaxRefFormatError.withPath(s"/$startOfPath/pensionSchemeTaxReference/$referenceIndex")))
        }
      }
    }

    combine(
      pensionContributions
        .traverse(pensionContributions => validateReferences("pensionContributions", pensionContributions.pensionSchemeTaxReference)),
      pensionSchemeUnauthorisedPayments
        .traverse(pensionSchemeUnauthorisedPayments =>
          validateReferences("pensionSchemeUnauthorisedPayments", pensionSchemeUnauthorisedPayments.pensionSchemeTaxReference)),
      pensionSchemeOverseasTransfers
        .traverse(pensionSchemeOverseasTransfers =>
          validatePensionSchemeTaxRef("pensionSchemeOverseasTransfers", pensionSchemeOverseasTransfers.overseasSchemeProvider)),
      overseasPensionContributions
        .traverse(overseasPensionContributions =>
          validatePensionSchemeTaxRef("overseasPensionContributions", overseasPensionContributions.overseasSchemeProvider))
    )
  }

  private def validateRuleIsAnnualAllowanceReduced(
      maybePensionContributions: Option[request.PensionContributions]): Validated[Seq[MtdError], Unit] = {
    maybePensionContributions
      .map { pensionContributions =>
        import pensionContributions._
        isAnnualAllowanceReduced
          .map { isAnnualAllowanceReduced =>
            (isAnnualAllowanceReduced, taperedAnnualAllowance, moneyPurchasedAllowance) match {
              case (false, _, _)         => valid
              case (true, Some(true), _) => valid
              case (true, _, Some(true)) => valid
              case _                     => Invalid(List(RuleIsAnnualAllowanceReducedError))
            }
          }
          .getOrElse(valid)
      }
      .getOrElse(valid)
  }

  private def validateCharges(pensionCharges: request.Def2_CreateAmendPensionChargesRequestBody): Validated[Seq[MtdError], Unit] = {
    import pensionCharges._

    val fieldsWithPaths = List(
      (pensionSchemeOverseasTransfers.map(_.transferChargeTaxPaid), "/pensionSchemeOverseasTransfers/transferChargeTaxPaid"),
      (pensionSchemeOverseasTransfers.map(_.transferCharge), "/pensionSchemeOverseasTransfers/transferCharge"),
      (pensionSchemeUnauthorisedPayments.flatMap(_.surcharge.map(_.amount)), "/pensionSchemeUnauthorisedPayments/surcharge/amount"),
      (pensionSchemeUnauthorisedPayments.flatMap(_.surcharge.map(_.foreignTaxPaid)), "/pensionSchemeUnauthorisedPayments/surcharge/foreignTaxPaid"),
      (pensionSchemeUnauthorisedPayments.flatMap(_.noSurcharge.map(_.amount)), "/pensionSchemeUnauthorisedPayments/noSurcharge/amount"),
      (
        pensionSchemeUnauthorisedPayments.flatMap(_.noSurcharge.map(_.foreignTaxPaid)),
        "/pensionSchemeUnauthorisedPayments/noSurcharge/foreignTaxPaid"),
      (pensionContributions.map(_.annualAllowanceTaxPaid), "/pensionContributions/annualAllowanceTaxPaid"),
      (pensionContributions.map(_.inExcessOfTheAnnualAllowance), "/pensionContributions/inExcessOfTheAnnualAllowance"),
      (overseasPensionContributions.map(_.shortServiceRefund), "/overseasPensionContributions/shortServiceRefund"),
      (overseasPensionContributions.map(_.shortServiceRefundTaxPaid), "/overseasPensionContributions/shortServiceRefundTaxPaid")
    )

    val validateNumberFields = fieldsWithPaths
      .map {
        case (None, _)            => valid
        case (Some(number), path) => resolveParsedNumber(number, path)
      }
    validateNumberFields.sequence.andThen(_ => valid)

  }

  private def validateCountryCodes(pensionCharges: request.Def2_CreateAmendPensionChargesRequestBody): Validated[Seq[MtdError], Unit] = {
    import pensionCharges._

    def validateCountryCode(startOfPath: String, overseasSchemeProviders: Seq[request.OverseasSchemeProvider]): Validated[Seq[MtdError], Unit] = {
      overseasSchemeProviders.zipWithIndex.traverse_ { case (schemeProviderWithIndex, index) =>
        ResolveParsedCountryCode(schemeProviderWithIndex.providerCountryCode, s"/$startOfPath/overseasSchemeProvider/$index/providerCountryCode")
      }
    }

    combine(
      pensionSchemeOverseasTransfers
        .traverse(pensionSchemeOverseasTransfers =>
          validateCountryCode("pensionSchemeOverseasTransfers", pensionSchemeOverseasTransfers.overseasSchemeProvider)),
      overseasPensionContributions
        .traverse(overseasPensionContributions =>
          validateCountryCode("overseasPensionContributions", overseasPensionContributions.overseasSchemeProvider))
    )

  }

}
