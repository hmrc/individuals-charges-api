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

package v2.createAmend

import api.controllers.validators.resolvers.ResolveTaxYear
import api.models.domain.TaxYear

sealed trait CreateAmendPensionChargesSchema

object CreateAmendPensionChargesSchema {

  case object Def1 extends CreateAmendPensionChargesSchema
  case object Def2 extends CreateAmendPensionChargesSchema

  private val defaultSchema = Def1

  def schemaFor(taxYear: String): CreateAmendPensionChargesSchema =
    ResolveTaxYear(taxYear)
      .map(schemaFor)
      .getOrElse(defaultSchema)

  def schemaFor(taxYear: TaxYear): CreateAmendPensionChargesSchema = {
    if (taxYear.year >= TaxYear.starting(2024).year) Def2
    else Def1
  }

}
