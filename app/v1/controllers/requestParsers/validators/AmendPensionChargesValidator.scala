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
import v1.controllers.requestParsers.validators.validations._
import v1.models.des.OverseasSchemeProvider
import v1.models.errors.{CountryCodeFormatError, MtdError, RuleCountryCodeError, RuleIncorrectOrEmptyBodyError, TaxYearFormatError}
import v1.models.requestData.{AmendPensionChargesRawData, PensionCharges}

class AmendPensionChargesValidator @Inject()(appConfig: AppConfig) extends Validator[AmendPensionChargesRawData] {

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
        validateCharges(model) ++ validateCountryCodes(model)

      } else {
        List(RuleIncorrectOrEmptyBodyError)
      }
    )
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
    if(errors.nonEmpty){
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

    val pensionSchemeOverseasTransfersCountryErrors: List[MtdError] = pensionCharges.pensionSchemeOverseasTransfers.map{
      pensionSchemeOverseasTransfers =>
        countryCodeErrors("pensionSchemeOverseasTransfers",pensionSchemeOverseasTransfers.overseasSchemeProvider)
    }.getOrElse(NoValidationErrors)

    val overseasPensionContributionsCountryErrors: List[MtdError] = pensionCharges.overseasPensionContributions.map{
      overseasPensionContributions =>
        countryCodeErrors("overseasPensionContributions", overseasPensionContributions.overseasSchemeProvider)
    }.getOrElse(NoValidationErrors)

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
