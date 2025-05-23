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

package v3.pensionCharges.createAmend

import play.api.libs.json.JsValue
import shared.config.SharedAppConfig
import shared.controllers.validators.Validator
import CreateAmendPensionChargesSchema.{Def1, Def2}
import v3.pensionCharges.createAmend.def1.model.Def1_CreateAmendPensionChargesValidator
import v3.pensionCharges.createAmend.def2.model.Def2_CreateAmendPensionChargesValidator
import v3.pensionCharges.createAmend.model.request.CreateAmendPensionChargesRequestData

import javax.inject.Inject

class CreateAmendPensionChargesValidatorFactory @Inject() (implicit appConfig: SharedAppConfig) {

  def validator(nino: String, taxYear: String, body: JsValue): Validator[CreateAmendPensionChargesRequestData] = {
    val schema = CreateAmendPensionChargesSchema.schemaFor(taxYear)
    schema match {
      case Def1 => new Def1_CreateAmendPensionChargesValidator(nino, taxYear, body)
      case Def2 => new Def2_CreateAmendPensionChargesValidator(nino, taxYear, body)
    }
  }

}
