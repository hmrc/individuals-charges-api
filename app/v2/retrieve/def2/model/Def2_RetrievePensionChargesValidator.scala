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

package v2.retrieve.def2.model

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.{ResolveNino, ResolveTaxYear}
import api.models.errors.MtdError
import cats.data.Validated
import cats.implicits.catsSyntaxTuple2Semigroupal
import v2.retrieve.def2.model.request.Def2_RetrievePensionChargesRequestData
import v2.retrieve.model.request.RetrievePensionChargesRequestData

class Def2_RetrievePensionChargesValidator(nino: String, taxYear: String)
    extends Validator[RetrievePensionChargesRequestData] {

  private lazy val minTaxYear = 24

  def validate: Validated[Seq[MtdError], RetrievePensionChargesRequestData] = {
    (
      ResolveNino(nino),
      ResolveTaxYear(minTaxYear, taxYear, None, None)
    ).mapN(Def2_RetrievePensionChargesRequestData)

  }

}
