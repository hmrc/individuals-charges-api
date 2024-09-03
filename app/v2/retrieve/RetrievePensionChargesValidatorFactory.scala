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

package v2.retrieve

import api.controllers.validators.Validator
import cats.data.Validated.{Invalid, Valid}
import config.{AppConfig, ChargesFeatureSwitches}
import v2.retrieve.RetrievePensionChargesSchema.{Def1, Def2}
import v2.retrieve.def1.model.Def1_RetrievePensionChargesValidator
import v2.retrieve.def2.model.Def2_RetrievePensionChargesValidator
import v2.retrieve.model.request.RetrievePensionChargesRequestData

import javax.inject.Inject

class RetrievePensionChargesValidatorFactory @Inject() (appConfig: AppConfig) {

  def validator(nino: String, taxYear: String): Validator[RetrievePensionChargesRequestData] = {

    val featureSwitches = ChargesFeatureSwitches()(appConfig)

    RetrievePensionChargesSchema.schemaFor(Some(taxYear)) match {
      case Valid(Def1) => new Def1_RetrievePensionChargesValidator(nino, taxYear)(appConfig)
      case Valid(Def2) if featureSwitches.isRemoveLifetimePensionEnabled => new Def2_RetrievePensionChargesValidator(nino, taxYear)
      case Invalid(errors) => Validator.returningErrors(errors)
      case _ => new Def1_RetrievePensionChargesValidator(nino, taxYear)(appConfig)
    }

  }

}
