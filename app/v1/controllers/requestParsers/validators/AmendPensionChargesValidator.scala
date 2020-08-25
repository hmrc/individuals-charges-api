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
import play.api.libs.json.JsValue
import play.api.mvc.AnyContentAsJson
import v1.controllers.requestParsers.validators.validations._
import v1.models.des.{OverseasSchemeProvider, PensionSchemeOverseasTransfers}
import v1.models.errors.{MtdError, RuleIncorrectOrEmptyBodyError, TaxYearFormatError}
import v1.models.requestData.{AmendPensionChargesRawData, AmendPensionChargesRequest, PensionCharges}

class AmendPensionChargesValidator@Inject()(appConfig: AppConfig) extends Validator[AmendPensionChargesRawData]{

  private val validationSet = List(parameterFormatValidation, bodyFormatValidator)

  private def parameterFormatValidation: AmendPensionChargesRawData => List[List[MtdError]] = { data =>

    val taxYearValidation = TaxYearValidation.validate(data.taxYear)

    val minTaxYearValidation = if(taxYearValidation.contains(TaxYearFormatError)){
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

    val validationErrors = JsonFormatValidation.validate[PensionCharges](data.body.json, RuleIncorrectOrEmptyBodyError)

    lazy val emptyModel : Boolean = data.body.json.asOpt[PensionCharges].exists(x => x.overseasPensionContributions.isEmpty
      || x.pensionContributions.isEmpty || x.pensionSavingsTaxCharges.isEmpty || x.pensionSchemeOverseasTransfers.isEmpty
      || x.pensionSchemeUnauthorisedPayments.isEmpty)

    val errors = if(validationErrors.nonEmpty) validationErrors else if(emptyModel) List(RuleIncorrectOrEmptyBodyError) else List()

    List(
      errors
    )
  }

  private def validateOverseasSchemeProvider(overseasSchemeProvider: OverseasSchemeProvider, arrayIndex: Int): List[MtdError] = {
      List(
        CountryCodeValidation.validate(overseasSchemeProvider.providerCountryCode).map(
          _.copy(paths = Some(Seq(s"/overseasSchemeProvider/$arrayIndex/providerCountryCode")))
        )
      ).flatten
  }



  override def validate(data: AmendPensionChargesRawData): List[MtdError] = run(validationSet, data)
}
