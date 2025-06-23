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

package v3.pensionCharges.retrieve

import shared.controllers.validators.resolvers.ResolveTaxYear
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import shared.schema.DownstreamReadable
import cats.data.Validated
import cats.data.Validated.Valid
import play.api.libs.json.Reads
import v3.pensionCharges.retrieve.def1.model.response.Def1_RetrievePensionChargesResponse
import v3.pensionCharges.retrieve.def2.model.response.Def2_RetrievePensionChargesResponse
import v3.pensionCharges.retrieve.model.response.RetrievePensionChargesResponse

sealed trait RetrievePensionChargesSchema extends DownstreamReadable[RetrievePensionChargesResponse]

object RetrievePensionChargesSchema {

  case object Def1 extends RetrievePensionChargesSchema {
    type DownstreamResp = Def1_RetrievePensionChargesResponse
    val connectorReads: Reads[DownstreamResp] = Def1_RetrievePensionChargesResponse.reads
  }

  case object Def2 extends RetrievePensionChargesSchema {
    type DownstreamResp = Def2_RetrievePensionChargesResponse
    val connectorReads: Reads[DownstreamResp] = Def2_RetrievePensionChargesResponse.reads
  }

  val schema: RetrievePensionChargesSchema = Def1

  def schemaFor(maybeTaxYear: Option[String]): Validated[Seq[MtdError], RetrievePensionChargesSchema] =
    maybeTaxYear match {
      case Some(taxYearString) => ResolveTaxYear(taxYearString) andThen schemaFor
      case None                => Valid(schema)
    }

  def schemaFor(taxYear: TaxYear): Validated[Seq[MtdError], RetrievePensionChargesSchema] = {
    taxYear.year match {
      case x if x < 2025 => Valid(Def1)
      case _             => Valid(Def2)
    }
  }

}
