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

package v3.winterFuelPayment.createAmend

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.{ResolveNino, ResolveNonEmptyJsonObject, ResolveTaxYearMinimum}
import api.models.domain.TaxYear
import api.models.errors.MtdError
import cats.data.Validated
import cats.implicits.catsSyntaxTuple3Semigroupal
import play.api.libs.json.JsValue
import v3.winterFuelPayment.createAmend.models.request.*

class CreateAmendWinterFuelPaymentValidator(nino: String, taxYear: String, body: JsValue, temporalValidationEnabled: Boolean)
    extends Validator[CreateAmendWinterFuelPaymentRequestData] {

  private val resolveJson = ResolveNonEmptyJsonObject.resolver[CreateAmendWinterFuelPaymentRequestBody]

  private val resolveTaxYear = ResolveTaxYearMinimum(
    TaxYear.fromMtd("2026-27"),
    allowIncompleteTaxYear = !temporalValidationEnabled
  )

  override def validate: Validated[Seq[MtdError], CreateAmendWinterFuelPaymentRequestData] =
    (
      ResolveNino(nino),
      resolveTaxYear(taxYear),
      resolveJson(body)
    ).mapN(CreateAmendWinterFuelPaymentRequestData.apply) andThen CreateAmendWinterFuelPaymentRulesValidator.validateBusinessRules

}
