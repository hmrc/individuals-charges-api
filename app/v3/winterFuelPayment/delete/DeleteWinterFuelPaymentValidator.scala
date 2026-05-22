/*
 * Copyright 2026 HM Revenue & Customs
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

package v3.winterFuelPayment.delete

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.{ResolveNino, ResolveTaxYearMinimum}
import api.models.domain.TaxYear
import api.models.errors.MtdError
import cats.data.Validated
import cats.implicits.*
import v3.winterFuelPayment.delete.model.request.DeleteWinterFuelPaymentRequestData

import javax.inject.{Inject, Singleton}

@Singleton
class DeleteWinterFuelPaymentValidator @Inject() (nino: String, taxYear: String) extends Validator[DeleteWinterFuelPaymentRequestData] {

  private val resolveTaxYear = ResolveTaxYearMinimum(TaxYear.fromMtd("2026-27"))

  override def validate: Validated[Seq[MtdError], DeleteWinterFuelPaymentRequestData] =
    (
      ResolveNino(nino),
      resolveTaxYear(taxYear)
    ).mapN(DeleteWinterFuelPaymentRequestData.apply)

}
