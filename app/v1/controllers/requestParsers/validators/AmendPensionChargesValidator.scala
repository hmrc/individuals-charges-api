/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.controllers.requestParsers.validators

import config.AppConfig
import javax.inject.Inject
import v1.models.des.PensionSavingsTaxCharges
import v1.controllers.requestParsers.validators.validations._
import v1.models.des.OverseasSchemeProvider
import v1.models.errors.{CountryCodeFormatError, MtdError, PensionSchemeTaxRefFormatError, ProviderNameFormatError,
  QOPSRefFormatError, RuleCountryCodeError, RuleIncorrectOrEmptyBodyError, TaxYearFormatError}
import v1.models.requestData.{AmendPensionChargesRawData, PensionCharges}

class AmendPensionChargesValidator @Inject()(appConfig: AppConfig) extends Validator[AmendPensionChargesRawData] {

  private val nameMaxLength = 105
  private val addressMaxLength = 250

  private val validationSet = List(parameterFormatValidation, bodyFormatValidator)

  private def parameterFormatValidation: AmendPensionChargesRawData => List[List[MtdError]] = { data =>

    val taxYearValidation = TaxYearValidation.validate(data.taxYear)

    val minTaxYearValidation = if (taxYearValidation.contains(TaxYearFormatError)) {
      Seq()
    } else {
      Seq(MinTaxYearValidation.validate(data.taxYear, appConfig.minTaxYearPensionCharge.toInt))
    }

    (List(
      NinoValidation.validate(data.nino),
      taxYearValidation
    ) ++ minTaxYearValidation).distinct
  }

  private def bodyFormatValidator: AmendPensionChargesRawData => List[List[MtdError]] = { data =>

    val validationErrors: List[MtdError] = JsonFormatValidation.validate[PensionCharges](data.body.json, RuleIncorrectOrEmptyBodyError)

    lazy val jsonAsModel: Option[PensionCharges] = data.body.json.asOpt[PensionCharges]

    lazy val nonEmptyModel: Boolean = jsonAsModel.exists(x => x.overseasPensionContributions.isDefined
      || x.pensionContributions.isDefined || x.pensionSavingsTaxCharges.isDefined || x.pensionSchemeOverseasTransfers.isDefined
      || x.pensionSchemeUnauthorisedPayments.isDefined)

    List(
      if (validationErrors.nonEmpty) {
        validationErrors
      } else if (nonEmptyModel) {

        val model = jsonAsModel.get
        validateCharges(model) ++
          validateCountryCodes(model) ++
          validateQROPsReferences(model) ++
          validatePensionSchemeTaxReference(model) ++
          validateNames(model) ++
          validateAddresses(model) ++
          validateRuleIsAnnualAllowanceReduced(model.pensionSavingsTaxCharges)
      } else {
        List(RuleIncorrectOrEmptyBodyError)
      }
    )
  }

  private def validateNames(pensionCharges: PensionCharges): List[MtdError] = {
    def namesErrors(startOfPath: String, overseasSchemeProviders: Seq[OverseasSchemeProvider]): List[MtdError] = {
      overseasSchemeProviders.zipWithIndex.flatMap {
        case (schemeProviderWithIndex, index) =>
          ProviderNameValidation.validate(schemeProviderWithIndex.providerName, nameMaxLength, s"/$startOfPath/overseasSchemeProvider/$index/providerName")
      }.toList
    }

    val pensionSchemeOverseasTransfersNameErrors: List[MtdError] = pensionCharges.pensionSchemeOverseasTransfers.map {
      pensionSchemeOverseasTransfers =>
        namesErrors("pensionSchemeOverseasTransfers", pensionSchemeOverseasTransfers.overseasSchemeProvider)
    }.getOrElse(NoValidationErrors)

    val overseasPensionContributionsNameErrors: List[MtdError] = pensionCharges.overseasPensionContributions.map {
      overseasPensionContributions =>
        namesErrors("overseasPensionContributions", overseasPensionContributions.overseasSchemeProvider)
    }.getOrElse(NoValidationErrors)

    val allNameErrors: List[MtdError] = pensionSchemeOverseasTransfersNameErrors ++ overseasPensionContributionsNameErrors
    lazy val paths = allNameErrors.flatMap(_.paths).flatten

    if(allNameErrors.nonEmpty) List(ProviderNameFormatError.copy(paths = Some(paths))) else NoValidationErrors
  }

  private def validateAddresses(pensionCharges: PensionCharges): List[MtdError] = {
    def addressesErrors(startOfPath: String, overseasSchemeProviders: Seq[OverseasSchemeProvider]): List[MtdError] = {
      overseasSchemeProviders.zipWithIndex.flatMap {
        case (schemeProviderWithIndex, index) =>
          ProviderAddressValidation.validate(schemeProviderWithIndex.providerAddress,
            addressMaxLength, s"/$startOfPath/overseasSchemeProvider/$index/providerAddress")
      }.toList
    }

    val pensionSchemeOverseasTransfersAddressesErrors: List[MtdError] = pensionCharges.pensionSchemeOverseasTransfers.map {
      pensionSchemeOverseasTransfers =>
        addressesErrors("pensionSchemeOverseasTransfers", pensionSchemeOverseasTransfers.overseasSchemeProvider)
    }.getOrElse(NoValidationErrors)

    val overseasPensionContributionsAddressesErrors: List[MtdError] = pensionCharges.overseasPensionContributions.map {
      overseasPensionContributions =>
        addressesErrors("overseasPensionContributions", overseasPensionContributions.overseasSchemeProvider)
    }.getOrElse(NoValidationErrors)

    val allAddressesErrors: List[MtdError] = pensionSchemeOverseasTransfersAddressesErrors ++ overseasPensionContributionsAddressesErrors
    lazy val paths = allAddressesErrors.flatMap(_.paths).flatten

    if(allAddressesErrors.nonEmpty) List(ProviderNameFormatError.copy(paths = Some(paths))) else NoValidationErrors
  }

  private def validateQROPsReferences(pensionCharges: PensionCharges): List[MtdError] = {
    def qropsErrors(startOfPath: String, overseasSchemeProviders: Seq[OverseasSchemeProvider]): List[MtdError] = {
      overseasSchemeProviders.zipWithIndex.flatMap {
        case (schemeProviderWithIndex, index) =>
          schemeProviderWithIndex.qualifyingRecognisedOverseasPensionScheme.map { qualifyingRecognisedOverseasPensionScheme =>
            qualifyingRecognisedOverseasPensionScheme.zipWithIndex.flatMap {
              case (qropsReference, qropsIndex) =>
                QROPSRefValidation.validate(
                  qropsReference, s"/$startOfPath/overseasSchemeProvider/$index/qualifyingRecognisedOverseasPensionScheme/$qropsIndex")
            }
          }.getOrElse(NoValidationErrors)
      }.toList
    }

    val pensionSchemeOverseasTransfersQROPsErrors: List[MtdError] = pensionCharges.pensionSchemeOverseasTransfers.map(pensionSchemeOverseasTransfers =>
      qropsErrors("pensionSchemeOverseasTransfers", pensionSchemeOverseasTransfers.overseasSchemeProvider)).getOrElse(NoValidationErrors)

    val overseasPensionContributionsQROPsErrors: List[MtdError] = pensionCharges.overseasPensionContributions.map(overseasPensionContributions =>
      qropsErrors("overseasPensionContributions", overseasPensionContributions.overseasSchemeProvider)).getOrElse(NoValidationErrors)

    val allQROPsErrors: List[MtdError] = pensionSchemeOverseasTransfersQROPsErrors ++ overseasPensionContributionsQROPsErrors
    lazy val paths = allQROPsErrors.flatMap(_.paths).flatten

    if(allQROPsErrors.nonEmpty) List(QOPSRefFormatError.copy(paths = Some(paths))) else NoValidationErrors
  }

  private def validatePensionSchemeTaxReference(pensionCharges: PensionCharges): List[MtdError] = {
    def pensionSchemeTaxReferenceErrors(startOfPath: String, overseasSchemeProviders: Seq[OverseasSchemeProvider]): List[MtdError] = {
      overseasSchemeProviders.zipWithIndex.flatMap {
        case (schemeProviderWithIndex, index) =>
          schemeProviderWithIndex.pensionSchemeTaxReference.map { references =>
            validateReferences(s"$startOfPath/overseasSchemeProvider/$index",references)
          }.getOrElse(NoValidationErrors)
      }.toList
    }

    def validateReferences(startOfPath: String, pensionSchemeTaxReference: Seq[String]): List[MtdError] ={
      pensionSchemeTaxReference.zipWithIndex.flatMap {
        case (reference, referenceIndex) =>
          PensionSchemeTaxReferenceValidation.validate(reference, s"/$startOfPath/pensionSchemeTaxReference/$referenceIndex")
      }.toList
    }

    val pensionContributionsReferencesErrors: List[MtdError] = pensionCharges.pensionContributions.map(pensionContributions =>
      validateReferences("pensionContributions", pensionContributions.pensionSchemeTaxReference)).getOrElse(NoValidationErrors)

    val pensionSavingsTaxChargesReferencesErrors: List[MtdError] = pensionCharges.pensionSavingsTaxCharges.map(pensionSavingsTaxCharges =>
      validateReferences("pensionSavingsTaxCharges", pensionSavingsTaxCharges.pensionSchemeTaxReference)).getOrElse(NoValidationErrors)

    val pensionSchemeUnauthorisedPaymentsReferencesErrors: List[MtdError] = pensionCharges.pensionSchemeUnauthorisedPayments.map(
      pensionSchemeUnauthorisedPayments =>
        validateReferences("pensionSchemeUnauthorisedPayments", pensionSchemeUnauthorisedPayments.pensionSchemeTaxReference)
    ).getOrElse(NoValidationErrors)

    val pensionSchemeOverseasTransfersReferencesErrors: List[MtdError] = pensionCharges.pensionSchemeOverseasTransfers.map {
      pensionSchemeOverseasTransfers =>
        pensionSchemeTaxReferenceErrors("pensionSchemeOverseasTransfers", pensionSchemeOverseasTransfers.overseasSchemeProvider)
    }.getOrElse(NoValidationErrors)

    val overseasPensionContributionsReferencesErrors: List[MtdError] = pensionCharges.overseasPensionContributions.map {
      overseasPensionContributions =>
        pensionSchemeTaxReferenceErrors("overseasPensionContributions", overseasPensionContributions.overseasSchemeProvider)
    }.getOrElse(NoValidationErrors)

    val allReferencesErrors: List[MtdError] = {
      pensionSchemeOverseasTransfersReferencesErrors ++ overseasPensionContributionsReferencesErrors ++ pensionContributionsReferencesErrors ++
        pensionSavingsTaxChargesReferencesErrors ++ pensionSchemeUnauthorisedPaymentsReferencesErrors
    }
    lazy val paths = allReferencesErrors.flatMap(_.paths).flatten

    if(allReferencesErrors.nonEmpty) List(PensionSchemeTaxRefFormatError.copy(paths = Some(paths))) else NoValidationErrors
  }

  private def validateRuleIsAnnualAllowanceReduced(pensionSavingsTaxCharges: Option[PensionSavingsTaxCharges]): List[MtdError] = {
    pensionSavingsTaxCharges.map { pensionSavingsTaxCharges =>
      List(
        RuleIsAnnualAllowanceReducedValidation.validate(pensionSavingsTaxCharges.isAnnualAllowanceReduced,
          pensionSavingsTaxCharges.taperedAnnualAllowance, pensionSavingsTaxCharges.moneyPurchasedAllowance
        ),
        RuleBenefitExcessesValidation.validate(
          pensionSavingsTaxCharges.lumpSumBenefitTakenInExcessOfLifetimeAllowance, pensionSavingsTaxCharges.benefitInExcessOfLifetimeAllowance
        )
      ).flatten
    }.getOrElse(NoValidationErrors)
  }

  private def validateCharges(pensionCharges: PensionCharges): List[MtdError] = {
    val errors = List(
      NumberValidation.validateOptional(
        field = pensionCharges.pensionSavingsTaxCharges.flatMap(_.benefitInExcessOfLifetimeAllowance.map(_.amount)),
        path = s"/pensionSavingsTaxCharges/benefitInExcessOfLifetimeAllowance/amount"),
      NumberValidation.validateOptional(
        field = pensionCharges.pensionSavingsTaxCharges.flatMap(_.benefitInExcessOfLifetimeAllowance.map(_.taxPaid)),
        path = s"/pensionSavingsTaxCharges/benefitInExcessOfLifetimeAllowance/taxPaid"),
      NumberValidation.validateOptional(
        field = pensionCharges.pensionSavingsTaxCharges.flatMap(_.lumpSumBenefitTakenInExcessOfLifetimeAllowance.map(_.amount)),
        path = s"/pensionSavingsTaxCharges/lumpSumBenefitTakenInExcessOfLifetimeAllowance/amount"),
      NumberValidation.validateOptional(
        field = pensionCharges.pensionSavingsTaxCharges.flatMap(_.lumpSumBenefitTakenInExcessOfLifetimeAllowance.map(_.taxPaid)),
        path = s"/pensionSavingsTaxCharges/lumpSumBenefitTakenInExcessOfLifetimeAllowance/taxPaid"),
      NumberValidation.validateOptional(
        field = pensionCharges.pensionSchemeOverseasTransfers.map(_.transferChargeTaxPaid),
        path = s"/pensionSchemeOverseasTransfers/transferChargeTaxPaid"),
      NumberValidation.validateOptional(
        field = pensionCharges.pensionSchemeOverseasTransfers.map(_.transferCharge),
        path = s"/pensionSchemeOverseasTransfers/transferCharge"),
      NumberValidation.validateOptional(
        field = pensionCharges.pensionSchemeUnauthorisedPayments.flatMap(_.surcharge.map(_.amount)),
        path = s"/pensionSchemeUnauthorisedPayments/surcharge/amount"),
      NumberValidation.validateOptional(
        field = pensionCharges.pensionSchemeUnauthorisedPayments.flatMap(_.surcharge.map(_.foreignTaxPaid)),
        path = s"/pensionSchemeUnauthorisedPayments/surcharge/foreignTaxPaid"),
      NumberValidation.validateOptional(
        field = pensionCharges.pensionSchemeUnauthorisedPayments.flatMap(_.noSurcharge.map(_.amount)),
        path = s"/pensionSchemeUnauthorisedPayments/noSurcharge/amount"),
      NumberValidation.validateOptional(
        field = pensionCharges.pensionSchemeUnauthorisedPayments.flatMap(_.noSurcharge.map(_.foreignTaxPaid)),
        path = s"/pensionSchemeUnauthorisedPayments/noSurcharge/foreignTaxPaid"),
      NumberValidation.validateOptional(
        field = pensionCharges.pensionContributions.map(_.annualAllowanceTaxPaid),
        path = s"/pensionContributions/annualAllowanceTaxPaid"),
      NumberValidation.validateOptional(
        field = pensionCharges.pensionContributions.map(_.inExcessOfTheAnnualAllowance),
        path = s"/pensionContributions/inExcessOfTheAnnualAllowance"),
      NumberValidation.validateOptional(
        field = pensionCharges.overseasPensionContributions.map(_.shortServiceRefund),
        path = s"/overseasPensionContributions/shortServiceRefund"),
      NumberValidation.validateOptional(
        field = pensionCharges.overseasPensionContributions.map(_.shortServiceRefundTaxPaid),
        path = s"/overseasPensionContributions/shortServiceRefundTaxPaid")
    ).flatten
    if (errors.nonEmpty) {
      List(errors.head.copy(paths = Some(errors.flatMap(_.paths).flatten)))
    } else {
      NoValidationErrors
    }
  }

  private def validateCountryCodes(pensionCharges: PensionCharges): List[MtdError] = {
    def countryCodeErrors(startOfPath: String, overseasSchemeProviders: Seq[OverseasSchemeProvider]): List[MtdError] = {
      overseasSchemeProviders.zipWithIndex.flatMap {
        case (schemeProviderWithIndex, index) =>
          CountryCodeValidation.validate(schemeProviderWithIndex.providerCountryCode).map(
            _.copy(paths = Some(Seq(s"/$startOfPath/overseasSchemeProvider/$index/providerCountryCode"))))
      }.toList
    }

    val pensionSchemeOverseasTransfersCountryErrors: List[MtdError] = pensionCharges.pensionSchemeOverseasTransfers.map(pensionSchemeOverseasTransfers =>
        countryCodeErrors("pensionSchemeOverseasTransfers", pensionSchemeOverseasTransfers.overseasSchemeProvider)).getOrElse(NoValidationErrors)

    val overseasPensionContributionsCountryErrors: List[MtdError] = pensionCharges.overseasPensionContributions.map(overseasPensionContributions =>
        countryCodeErrors("overseasPensionContributions", overseasPensionContributions.overseasSchemeProvider)).getOrElse(NoValidationErrors)

    val allCountryCodeErrors: List[MtdError] = pensionSchemeOverseasTransfersCountryErrors ++ overseasPensionContributionsCountryErrors
    val countryCodeFormatPaths: Seq[String] = allCountryCodeErrors.filter(error => error.code.equals(CountryCodeFormatError.code)).flatMap(_.paths).flatten
    val ruleCountryCodePaths: Seq[String] = allCountryCodeErrors.filter(error => error.code.equals(RuleCountryCodeError.code)).flatMap(_.paths).flatten

    (countryCodeFormatPaths.nonEmpty, ruleCountryCodePaths.nonEmpty) match {
      case (true, true) => List(
        CountryCodeFormatError.copy(paths = Some(countryCodeFormatPaths)),
        RuleCountryCodeError.copy(paths = Some(ruleCountryCodePaths))
      )
      case (true, _) => List(CountryCodeFormatError.copy(paths = Some(countryCodeFormatPaths)))
      case (_, true) => List(RuleCountryCodeError.copy(paths = Some(ruleCountryCodePaths)))
      case _ => NoValidationErrors
    }
  }

  override def validate(data: AmendPensionChargesRawData): List[MtdError] = run(validationSet, data)
}
