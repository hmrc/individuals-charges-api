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

package v1.controllers.requestParsers.validators

import config.AppConfig
import javax.inject.Inject
import v1.controllers.requestParsers.validators.validations.{MinTaxYearValidation, NinoValidation, TaxYearValidation}
import v1.models.errors.{MtdError, TaxYearFormatError}
import v1.models.request.DeletePensionCharges.DeletePensionChargesRawData

class DeletePensionChargesValidator @Inject()(appConfig: AppConfig) extends Validator[DeletePensionChargesRawData]{

  private val validationSet = List(parameterFormatValidation)

  private def parameterFormatValidation: DeletePensionChargesRawData => List[List[MtdError]] = { data =>

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

  override def validate(data: DeletePensionChargesRawData): List[MtdError] = run(validationSet, data)
}
