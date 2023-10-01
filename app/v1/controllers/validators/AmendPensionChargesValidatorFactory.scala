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

package v1.controllers.validators

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.{ResolveJsonObject, ResolveNino, ResolveTaxYear}
import api.models.errors.MtdError
import cats.data.Validated
import cats.implicits._
import config.AppConfig
import play.api.libs.json.JsValue
import v1.controllers.validators.AmendPensionChargesRulesValidator.validateBusinessRules
import v1.models.request.AmendPensionCharges.{AmendPensionChargesRequestData, PensionCharges}

import javax.inject.Inject

class AmendPensionChargesValidatorFactory @Inject() (appConfig: AppConfig) {

  private lazy val minTaxYear = appConfig.minTaxYearPensionCharge.toInt

  private val resolveJson = new ResolveJsonObject[PensionCharges]()

  def validator(nino: String, taxYear: String, body: JsValue): Validator[AmendPensionChargesRequestData] =
    new Validator[AmendPensionChargesRequestData] {

      def validate: Validated[Seq[MtdError], AmendPensionChargesRequestData] =
        (
          ResolveNino(nino),
          ResolveTaxYear(minTaxYear, taxYear, None, None),
          resolveJson(body)
        ).mapN(AmendPensionChargesRequestData) andThen validateBusinessRules

    }

}
