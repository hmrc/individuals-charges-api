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

package v3.winterFuelPayment.retrieve

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.ResolverSupport.*
import api.controllers.validators.resolvers.{ResolveNino, ResolveTaxYearMinimum, ResolverSupport}
import api.models.domain.{MtdSourceEnum, TaxYear}
import api.models.errors.MtdError
import cats.data.Validated
import cats.implicits.*
import common.errors.SourceFormatError
import v3.winterFuelPayment.retrieve.model.request.RetrieveWinterFuelPaymentRequestData

import javax.inject.Inject

class RetrieveWinterFuelPaymentValidator @Inject() (nino: String, taxYear: String, maybeSource: Option[String])
    extends Validator[RetrieveWinterFuelPaymentRequestData]
    with ResolverSupport {

  private val resolveTaxYear = ResolveTaxYearMinimum(TaxYear.fromMtd("2026-27"))

  private val resolveSource: Resolver[Option[String], MtdSourceEnum] =
    resolvePartialFunction(SourceFormatError)(MtdSourceEnum.parser).resolveOptionallyWithDefault(MtdSourceEnum.latest)

  override def validate: Validated[Seq[MtdError], RetrieveWinterFuelPaymentRequestData] = {
    (
      ResolveNino(nino),
      resolveTaxYear(taxYear),
      resolveSource(maybeSource)
    ).mapN(RetrieveWinterFuelPaymentRequestData.apply)
  }

}
