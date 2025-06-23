/*
 * Copyright 2024 HM Revenue & Customs
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

package v3.pensionCharges.retrieve

import cats.data.Validated.{Invalid, Valid}
import config.ChargesFeatureSwitches
import shared.config.SharedAppConfig
import shared.controllers.validators.Validator
import v3.pensionCharges.retrieve.def1.model.Def1_RetrievePensionChargesValidator
import v3.pensionCharges.retrieve.def2.model.Def2_RetrievePensionChargesValidator
import v3.pensionCharges.retrieve.model.request.RetrievePensionChargesRequestData
import RetrievePensionChargesSchema.{Def1, Def2}

import javax.inject.Inject

class RetrievePensionChargesValidatorFactory @Inject() (appConfig: SharedAppConfig) {

  def validator(nino: String, taxYear: String): Validator[RetrievePensionChargesRequestData] = {

    val featureSwitches = ChargesFeatureSwitches()(appConfig)
    val default         = new Def1_RetrievePensionChargesValidator(nino, taxYear)

    RetrievePensionChargesSchema.schemaFor(Some(taxYear)) match {
      case Valid(Def1)                                                   => new Def1_RetrievePensionChargesValidator(nino, taxYear)
      case Valid(Def2) if featureSwitches.isRemoveLifetimePensionEnabled => new Def2_RetrievePensionChargesValidator(nino, taxYear)
      case Invalid(errors)                                               => Validator.returningErrors(errors)
      case _                                                             => default
    }

  }

}
