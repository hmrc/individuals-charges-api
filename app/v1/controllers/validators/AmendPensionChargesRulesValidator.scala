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
  private val resolveParsedNumber = ResolveParsedNumber()

  def validateBusinessRules(parsed: AmendPensionChargesRequestData): Validated[Seq[MtdError], AmendPensionChargesRequestData] = {

    import parsed._

    List(
      validateRulePensionReference(pensionCharges),
      validateNames(pensionCharges),
      validateAddresses(pensionCharges),
      validateQROPsReferences(pensionCharges),
      validatePensionSchemeTaxReference(pensionCharges),
      validateRuleIsAnnualAllowanceReduced(pensionCharges.pensionSavingsTaxCharges),
      validateCharges(pensionCharges),
      validateCountryCodes(pensionCharges)
    ).traverse(identity).map(_ => parsed)
  }

  private def validateRulePensionReference(pensionCharges: PensionCharges): Validated[Seq[MtdError], Unit] = {

    def rulePensionReferenceResolver(overseasSchemeProviders: Seq[OverseasSchemeProvider]): Validated[Seq[MtdError], Unit] =
      overseasSchemeProviders.traverse_(schemeProvider =>
        validateOverseasSchemeProvider(schemeProvider.qualifyingRecognisedOverseasPensionScheme, schemeProvider.pensionSchemeTaxReference))

    (
      pensionCharges.pensionSchemeOverseasTransfers
        .map(pensionSchemeOverseasTransfers => rulePensionReferenceResolver(pensionSchemeOverseasTransfers.overseasSchemeProvider))
        .getOrElse(valid),
      pensionCharges.overseasPensionContributions
        .map(overseasPensionContributions => rulePensionReferenceResolver(overseasPensionContributions.overseasSchemeProvider))
        .getOrElse(valid)
    ).tupled
      .andThen { case (_, _) => valid }
  }

  def validateOverseasSchemeProvider(qualifyingRecognisedOverseasPensionSchemeReferenceNumber: Option[Seq[String]],
                                     pensionSchemeTaxReference: Option[Seq[String]]): Validated[Seq[MtdError], Unit] =
    (qualifyingRecognisedOverseasPensionSchemeReferenceNumber, pensionSchemeTaxReference) match {
      case (Some(_), Some(_)) => Invalid(List(RulePensionReferenceError))
      case _                  => valid
    }

  private def validateNames(pensionCharges: PensionCharges): Validated[Seq[MtdError], Unit] = {
    def namesResolver(startOfPath: String, overseasSchemeProviders: Seq[OverseasSchemeProvider]): Validated[Seq[MtdError], Unit] = {
      overseasSchemeProviders.zipWithIndex.traverse_ { case (schemeProviderWithIndex, index) =>
        validateProviderName(schemeProviderWithIndex.providerName, s"/$startOfPath/overseasSchemeProvider/$index/providerName")
      }
    }
    (
      pensionCharges.pensionSchemeOverseasTransfers
        .map(pensionSchemeOverseasTransfers => namesResolver("pensionSchemeOverseasTransfers", pensionSchemeOverseasTransfers.overseasSchemeProvider))
        .getOrElse(valid),
      pensionCharges.overseasPensionContributions
        .map(overseasPensionContributions => namesResolver("overseasPensionContributions", overseasPensionContributions.overseasSchemeProvider))
        .getOrElse(valid)
    ).tupled
      .andThen { case (_, _) => valid }

  }

  def validateProviderName(providerName: String, path: String): Validated[Seq[MtdError], Unit] = {
    val nameMaxLength = 105
    if (providerName.length() <= nameMaxLength && providerName.nonEmpty) { valid }
    else { Invalid(List(ProviderNameFormatError.copy(paths = Some(List(path))))) }
  }

  private def validateAddresses(pensionCharges: PensionCharges): Validated[Seq[MtdError], Unit] = {
    def addressesErrors(startOfPath: String, overseasSchemeProviders: Seq[OverseasSchemeProvider]): Validated[Seq[MtdError], Unit] = {
      overseasSchemeProviders.zipWithIndex.traverse_ { case (schemeProviderWithIndex, index) =>
        validateProviderAddress(schemeProviderWithIndex.providerAddress, s"/$startOfPath/overseasSchemeProvider/$index/providerAddress")
      }
    }
    (
      pensionCharges.pensionSchemeOverseasTransfers
        .map(pensionSchemeOverseasTransfers =>
          addressesErrors("pensionSchemeOverseasTransfers", pensionSchemeOverseasTransfers.overseasSchemeProvider))
        .getOrElse(valid),
      pensionCharges.overseasPensionContributions
        .map(overseasPensionContributions => addressesErrors("overseasPensionContributions", overseasPensionContributions.overseasSchemeProvider))
        .getOrElse(valid)
    ).tupled
      .andThen { case (_, _) => valid }

  }

  def validateProviderAddress(providerAddress: String, path: String): Validated[Seq[MtdError], Unit] = {
    val addressMaxLength = 250
    if (providerAddress.length() <= addressMaxLength && providerAddress.nonEmpty) {
      valid
    } else {
      Invalid(List(ProviderAddressFormatError.copy(paths = Some(Seq(path)))))
    }
  }

  private def validateQROPsReferences(pensionCharges: PensionCharges): Validated[Seq[MtdError], Unit] = {
    def qropsResolver(startOfPath: String, overseasSchemeProviders: Seq[OverseasSchemeProvider]): Validated[Seq[MtdError], Unit] = {
      overseasSchemeProviders.zipWithIndex.traverse_ { case (schemeProviderWithIndex, index) =>
        schemeProviderWithIndex.qualifyingRecognisedOverseasPensionScheme
          .map { qualifyingRecognisedOverseasPensionScheme =>
            qualifyingRecognisedOverseasPensionScheme.zipWithIndex.traverse_ { case (qropsReference, qropsIndex) =>
              validateQropsRef(qropsReference, s"/$startOfPath/overseasSchemeProvider/$index/qualifyingRecognisedOverseasPensionScheme/$qropsIndex")
            }
          }
          .getOrElse(valid)
      }
    }

    (
      pensionCharges.pensionSchemeOverseasTransfers
        .map(pensionSchemeOverseasTransfers => qropsResolver("pensionSchemeOverseasTransfers", pensionSchemeOverseasTransfers.overseasSchemeProvider))
        .getOrElse(valid),
      pensionCharges.overseasPensionContributions
        .map(overseasPensionContributions => qropsResolver("overseasPensionContributions", overseasPensionContributions.overseasSchemeProvider))
        .getOrElse(valid)
    ).tupled
      .andThen { case (_, _) => valid }

  }

  def validateQropsRef(qropsRef: String, path: String): Validated[Seq[MtdError], Unit] = {
    if (qropsRef.matches("^[Q]{1}[0-9]{6}$")) {
      valid
    } else {
      Invalid(List(QOPSRefFormatError.copy(paths = Some(Seq(path)))))
    }
  }

  private def validatePensionSchemeTaxReference(pensionCharges: PensionCharges): Validated[Seq[MtdError], Unit] = {
    def pensionSchemeTaxReferenceResolver(startOfPath: String,
                                          overseasSchemeProviders: Seq[OverseasSchemeProvider]): Validated[Seq[MtdError], Unit] = {
      overseasSchemeProviders.zipWithIndex.traverse_ { case (schemeProviderWithIndex, index) =>
        schemeProviderWithIndex.pensionSchemeTaxReference
          .map { references =>
            validateReferences(s"$startOfPath/overseasSchemeProvider/$index", references)
          }
          .getOrElse(valid)
      }
    }

    def validateReferences(startOfPath: String, pensionSchemeTaxReference: Seq[String]): Validated[Seq[MtdError], Unit] = {
      pensionSchemeTaxReference.zipWithIndex.traverse_ { case (reference, referenceIndex) =>
        validatePensionSchemeTaxRef(reference, s"/$startOfPath/pensionSchemeTaxReference/$referenceIndex")
      }
    }
    (
      pensionCharges.pensionContributions
        .map(pensionContributions => validateReferences("pensionContributions", pensionContributions.pensionSchemeTaxReference))
        .getOrElse(valid),
      pensionCharges.pensionSavingsTaxCharges
        .map(pensionSavingsTaxCharges => validateReferences("pensionSavingsTaxCharges", pensionSavingsTaxCharges.pensionSchemeTaxReference))
        .getOrElse(valid),
      pensionCharges.pensionSchemeUnauthorisedPayments
        .map(pensionSchemeUnauthorisedPayments =>
          validateReferences("pensionSchemeUnauthorisedPayments", pensionSchemeUnauthorisedPayments.pensionSchemeTaxReference))
        .getOrElse(valid),
      pensionCharges.pensionSchemeOverseasTransfers
        .map(pensionSchemeOverseasTransfers =>
          pensionSchemeTaxReferenceResolver("pensionSchemeOverseasTransfers", pensionSchemeOverseasTransfers.overseasSchemeProvider))
        .getOrElse(valid),
      pensionCharges.overseasPensionContributions
        .map(overseasPensionContributions =>
          pensionSchemeTaxReferenceResolver("overseasPensionContributions", overseasPensionContributions.overseasSchemeProvider))
        .getOrElse(valid)
    ).tupled
      .andThen { case (_, _, _, _, _) => valid }
  }

  def validatePensionSchemeTaxRef(pensionSchemeTaxRef: String, path: String): Validated[Seq[MtdError], Unit] = {
    val regex = "^\\d{8}[R]{1}[a-zA-Z]{1}$"
    if (pensionSchemeTaxRef.matches(regex)) {
      valid
    } else {
      Invalid(
        List(
          PensionSchemeTaxRefFormatError.copy(paths = Some(Seq(path)))
        ))
    }
  }

  private def validateRuleIsAnnualAllowanceReduced(pensionSavingsTaxCharges: Option[PensionSavingsTaxCharges]): Validated[Seq[MtdError], Unit] = {
    pensionSavingsTaxCharges
      .map { taxCharges =>
        taxCharges.isAnnualAllowanceReduced
          .map { isAnnualAllowanceReduced =>
            validateAllowance(isAnnualAllowanceReduced, taxCharges.taperedAnnualAllowance, taxCharges.moneyPurchasedAllowance)
          }
          .getOrElse(Invalid(List(RuleIncorrectOrEmptyBodyError)))
      }
      .getOrElse(valid)
  }

  def validateAllowance(isAnnualAllowanceReduced: Boolean,
                        taperedAnnualAllowance: Option[Boolean],
                        moneyPurchasedAllowance: Option[Boolean]): Validated[Seq[MtdError], Unit] =
    (isAnnualAllowanceReduced, taperedAnnualAllowance, moneyPurchasedAllowance) match {
      case (false, _, _)         => valid
      case (true, Some(true), _) => valid
      case (true, _, Some(true)) => valid
      case _                     => Invalid(List(RuleIsAnnualAllowanceReducedError))
    }

  private def validateCharges(pensionCharges: PensionCharges): Validated[Seq[MtdError], Unit] = {

    val fieldsWithPaths = List(
      (
        pensionCharges.pensionSavingsTaxCharges.flatMap(_.benefitInExcessOfLifetimeAllowance.map(_.amount)),
        s"/pensionSavingsTaxCharges/benefitInExcessOfLifetimeAllowance/amount"),
      (
        pensionCharges.pensionSavingsTaxCharges.flatMap(_.benefitInExcessOfLifetimeAllowance.map(_.taxPaid)),
        s"/pensionSavingsTaxCharges/benefitInExcessOfLifetimeAllowance/taxPaid"),
      (
        pensionCharges.pensionSavingsTaxCharges.flatMap(_.lumpSumBenefitTakenInExcessOfLifetimeAllowance.map(_.amount)),
        s"/pensionSavingsTaxCharges/lumpSumBenefitTakenInExcessOfLifetimeAllowance/amount"),
      (
        pensionCharges.pensionSavingsTaxCharges.flatMap(_.lumpSumBenefitTakenInExcessOfLifetimeAllowance.map(_.taxPaid)),
        s"/pensionSavingsTaxCharges/lumpSumBenefitTakenInExcessOfLifetimeAllowance/taxPaid"),
      (pensionCharges.pensionSchemeOverseasTransfers.map(_.transferChargeTaxPaid), s"/pensionSchemeOverseasTransfers/transferChargeTaxPaid"),
      (pensionCharges.pensionSchemeOverseasTransfers.map(_.transferCharge), s"/pensionSchemeOverseasTransfers/transferCharge"),
      (pensionCharges.pensionSchemeUnauthorisedPayments.flatMap(_.surcharge.map(_.amount)), s"/pensionSchemeUnauthorisedPayments/surcharge/amount"),
      (
        pensionCharges.pensionSchemeUnauthorisedPayments.flatMap(_.surcharge.map(_.foreignTaxPaid)),
        s"/pensionSchemeUnauthorisedPayments/surcharge/foreignTaxPaid"),
      (
        pensionCharges.pensionSchemeUnauthorisedPayments.flatMap(_.noSurcharge.map(_.amount)),
        s"/pensionSchemeUnauthorisedPayments/noSurcharge/amount"),
      (
        pensionCharges.pensionSchemeUnauthorisedPayments.flatMap(_.noSurcharge.map(_.foreignTaxPaid)),
        s"/pensionSchemeUnauthorisedPayments/noSurcharge/foreignTaxPaid"),
      (pensionCharges.pensionContributions.map(_.annualAllowanceTaxPaid), s"/pensionContributions/annualAllowanceTaxPaid"),
      (pensionCharges.pensionContributions.map(_.inExcessOfTheAnnualAllowance), s"/pensionContributions/inExcessOfTheAnnualAllowance"),
      (pensionCharges.overseasPensionContributions.map(_.shortServiceRefund), s"/overseasPensionContributions/shortServiceRefund"),
      (pensionCharges.overseasPensionContributions.map(_.shortServiceRefundTaxPaid), s"/overseasPensionContributions/shortServiceRefundTaxPaid")
    )

    val validateNumberFields = fieldsWithPaths
      .map {
        case (None, _)            => valid
        case (Some(number), path) => resolveParsedNumber(number, None, Some(path))
      }
    validateNumberFields.sequence.andThen(_ => valid)

  }

  private def validateCountryCodes(pensionCharges: PensionCharges): Validated[Seq[MtdError], Unit] = {
    def countryCodeResolver(startOfPath: String, overseasSchemeProviders: Seq[OverseasSchemeProvider]): Validated[Seq[MtdError], Unit] = {
      overseasSchemeProviders.zipWithIndex.traverse_ { case (schemeProviderWithIndex, index) =>
        ResolveParsedCountryCode(schemeProviderWithIndex.providerCountryCode, s"/$startOfPath/overseasSchemeProvider/$index/providerCountryCode")
      }
    }

    (
      pensionCharges.pensionSchemeOverseasTransfers
        .map(pensionSchemeOverseasTransfers =>
          countryCodeResolver("pensionSchemeOverseasTransfers", pensionSchemeOverseasTransfers.overseasSchemeProvider))
        .getOrElse(valid),
      pensionCharges.overseasPensionContributions
        .map(overseasPensionContributions => countryCodeResolver("overseasPensionContributions", overseasPensionContributions.overseasSchemeProvider))
        .getOrElse(valid)
    ).tupled
      .andThen { case (_, _) => valid }

  }

}
