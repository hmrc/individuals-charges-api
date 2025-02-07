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

package v3.retrieve.def1.model.response

import play.api.libs.json._
import v3.retrieve.model.response.RetrievePensionChargesResponse

case class Def1_RetrievePensionChargesResponse(submittedOn: String,
                                               pensionSavingsTaxCharges: Option[PensionSavingsTaxCharges],
                                               pensionSchemeOverseasTransfers: Option[PensionSchemeOverseasTransfers],
                                               pensionSchemeUnauthorisedPayments: Option[PensionSchemeUnauthorisedPayments],
                                               pensionContributions: Option[PensionContributions],
                                               overseasPensionContributions: Option[OverseasPensionContributions])
    extends RetrievePensionChargesResponse

object Def1_RetrievePensionChargesResponse {

  implicit val reads: Reads[Def1_RetrievePensionChargesResponse] = Json.reads[Def1_RetrievePensionChargesResponse]

  implicit val writes: OWrites[Def1_RetrievePensionChargesResponse] = Json.writes[Def1_RetrievePensionChargesResponse]

}
