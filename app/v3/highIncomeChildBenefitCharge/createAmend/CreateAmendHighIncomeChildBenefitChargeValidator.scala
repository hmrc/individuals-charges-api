/*
 * Copyright 2025 HM Revenue & Customs
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

package v3.highIncomeChildBenefitCharge.createAmend

import cats.data.Validated
import cats.implicits.catsSyntaxTuple3Semigroupal
import play.api.libs.json.JsValue
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveNino, ResolveNonEmptyJsonObject, ResolveTaxYearMinimum}
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import v3.highIncomeChildBenefitCharge.createAmend.models.request.*

class CreateAmendHighIncomeChildBenefitChargeValidator(nino: String, taxYear: String, body: JsValue)
    extends Validator[CreateAmendHighIncomeChildBenefitChargeRequest] {

  private val resolveJson = ResolveNonEmptyJsonObject.resolver[CreateAmendHighIncomeChildBenefitChargeRequestBody]

  private val resolveTaxYear = ResolveTaxYearMinimum(TaxYear.fromMtd("2025-26"))

  override def validate: Validated[Seq[MtdError], CreateAmendHighIncomeChildBenefitChargeRequest] =
    (
      ResolveNino(nino),
      resolveTaxYear(taxYear),
      resolveJson(body)
    ).mapN(CreateAmendHighIncomeChildBenefitChargeRequest.apply) andThen CreateAmendHighIncomeChildBenefitChargeRulesValidator.validateBusinessRules

}
