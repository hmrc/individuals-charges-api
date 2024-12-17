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

package v2.delete

import api.controllers.validators.Validator
import config.IndividualsChargesConfig
import v2.delete.DeletePensionChargesSchema.Def1
import v2.delete.def1.Def1_DeletePensionChargesValidator
import v2.delete.model.request.DeletePensionChargesRequestData

import javax.inject.{Inject, Singleton}

@Singleton
class DeletePensionChargesValidatorFactory @Inject() (appConfig: IndividualsChargesConfig) {
  
  def validator(nino: String, taxYear: String): Validator[DeletePensionChargesRequestData] = {

    val schema = DeletePensionChargesSchema.schema

    schema match {
      case Def1 => new Def1_DeletePensionChargesValidator(nino, taxYear)(appConfig)
    }
  }

}
