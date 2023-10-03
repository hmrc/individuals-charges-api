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

import api.controllers.validators.RulesValidator
import api.controllers.validators.resolvers.{ResolveParsedCountryCode, ResolveParsedNumber}
import api.models.errors._
import cats.data.Validated
import cats.data.Validated._
import cats.implicits._
import v1.models.request.AmendPensionCharges.{AmendPensionChargesRequestData, OverseasSchemeProvider, PensionCharges, PensionSavingsTaxCharges}

object AmendPensionChargesRulesValidator extends RulesValidator[AmendPensionChargesRequestData] {
  private val resolveParsedNumber      = ResolveParsedNumber()
  private val qropsRefRegex            = "^[Q]{1}[0-9]{6}$".r
  private val pensionSchemeTaxRefRegex = "^\\d{8}[R]{1}[a-zA-Z]{1}$".r

  def validateBusinessRules(parsed: AmendPensionChargesRequestData): Validated[Seq[MtdError], AmendPensionChargesRequestData] = {

    import parsed._

    combine(
      validateRulePensionReference(pensionCharges),
      validateNames(pensionCharges),
      validateAddresses(pensionCharges),
      validateQROPsReferences(pensionCharges),
      validatePensionSchemeTaxReference(pensionCharges),
      validateRuleIsAnnualAllowanceReduced(pensionCharges.pensionSavingsTaxCharges),
      validateCharges(pensionCharges),
      validateCountryCodes(pensionCharges)
    ).onSuccess(parsed)
  }

  private def validateRulePensionReference(pensionCharges: PensionCharges): Validated[Seq[MtdError], Unit] = {
    import pensionCharges._

    def validatePensionReference(overseasSchemeProviders: Seq[OverseasSchemeProvider]): Validated[Seq[MtdError], Unit] =
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

  private def validateNames(pensionCharges: PensionCharges): Validated[Seq[MtdError], Unit] = {
    import pensionCharges._

    def validateProviderName(startOfPath: String, overseasSchemeProviders: Seq[OverseasSchemeProvider]): Validated[Seq[MtdError], Unit] = {
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

  private def validateAddresses(pensionCharges: PensionCharges): Validated[Seq[MtdError], Unit] = {
    import pensionCharges._

    def validateProviderAddress(startOfPath: String, overseasSchemeProviders: Seq[OverseasSchemeProvider]): Validated[Seq[MtdError], Unit] = {
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

  private def validateQROPsReferences(pensionCharges: PensionCharges): Validated[Seq[MtdError], Unit] = {
    import pensionCharges._

    def validateQropsRef(startOfPath: String, overseasSchemeProviders: Seq[OverseasSchemeProvider]): Validated[Seq[MtdError], Unit] = {
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

  private def validatePensionSchemeTaxReference(pensionCharges: PensionCharges): Validated[Seq[MtdError], Unit] = {
    import pensionCharges._

    def validatePensionSchemeTaxRef(startOfPath: String, overseasSchemeProviders: Seq[OverseasSchemeProvider]): Validated[Seq[MtdError], Unit] = {
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
      pensionSavingsTaxCharges
        .traverse(pensionSavingsTaxCharges => validateReferences("pensionSavingsTaxCharges", pensionSavingsTaxCharges.pensionSchemeTaxReference)),
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

  private def validateRuleIsAnnualAllowanceReduced(pensionSavingsTaxCharges: Option[PensionSavingsTaxCharges]): Validated[Seq[MtdError], Unit] = {
    pensionSavingsTaxCharges
      .map { taxCharges =>
        import taxCharges._
        isAnnualAllowanceReduced
          .map { isAnnualAllowanceReduced =>
            (isAnnualAllowanceReduced, taperedAnnualAllowance, moneyPurchasedAllowance) match {
              case (false, _, _)         => valid
              case (true, Some(true), _) => valid
              case (true, _, Some(true)) => valid
              case _                     => Invalid(List(RuleIsAnnualAllowanceReducedError))
            }
          }
          .getOrElse(Invalid(List(RuleIncorrectOrEmptyBodyError)))
      }
      .getOrElse(valid)
  }

  private def validateCharges(pensionCharges: PensionCharges): Validated[Seq[MtdError], Unit] = {
    import pensionCharges._

    val fieldsWithPaths = List(
      (
        pensionSavingsTaxCharges.flatMap(_.benefitInExcessOfLifetimeAllowance.map(_.amount)),
        "/pensionSavingsTaxCharges/benefitInExcessOfLifetimeAllowance/amount"),
      (
        pensionSavingsTaxCharges.flatMap(_.benefitInExcessOfLifetimeAllowance.map(_.taxPaid)),
        "/pensionSavingsTaxCharges/benefitInExcessOfLifetimeAllowance/taxPaid"),
      (
        pensionSavingsTaxCharges.flatMap(_.lumpSumBenefitTakenInExcessOfLifetimeAllowance.map(_.amount)),
        "/pensionSavingsTaxCharges/lumpSumBenefitTakenInExcessOfLifetimeAllowance/amount"),
      (
        pensionSavingsTaxCharges.flatMap(_.lumpSumBenefitTakenInExcessOfLifetimeAllowance.map(_.taxPaid)),
        "/pensionSavingsTaxCharges/lumpSumBenefitTakenInExcessOfLifetimeAllowance/taxPaid"),
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
        case (Some(number), path) => resolveParsedNumber(number, None, Some(path))
      }
    validateNumberFields.sequence.andThen(_ => valid)

  }

  private def validateCountryCodes(pensionCharges: PensionCharges): Validated[Seq[MtdError], Unit] = {
    import pensionCharges._

    def validateCountryCode(startOfPath: String, overseasSchemeProviders: Seq[OverseasSchemeProvider]): Validated[Seq[MtdError], Unit] = {
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
