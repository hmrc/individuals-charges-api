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

package v2.delete.def1

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.{ResolveNino, ResolveTaxYear}
import api.models.errors.MtdError
import cats.data.Validated
import cats.implicits._
import config.AppConfig
import v2.delete.def1.request.Def1_DeletePensionChargesRequestData
import v2.delete.model.request.DeletePensionChargesRequestData

import javax.inject.{Inject, Singleton}

@Singleton
class Def1_DeletePensionChargesValidator @Inject() (nino: String, taxYear: String)(appConfig: AppConfig)
    extends Validator[DeletePensionChargesRequestData] {

  private lazy val minTaxYear = appConfig.minTaxYearPensionCharge.toInt

  def validate: Validated[Seq[MtdError], DeletePensionChargesRequestData] =
    (
      ResolveNino(nino),
      ResolveTaxYear(minTaxYear, taxYear, None, None)
    ).mapN(Def1_DeletePensionChargesRequestData)

}
