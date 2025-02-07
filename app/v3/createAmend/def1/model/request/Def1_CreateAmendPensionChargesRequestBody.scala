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

package v3.createAmend.def1.model.request

import play.api.libs.json.{Json, OFormat}
import v3.createAmend.model.request.CreateAmendPensionChargesRequestBody

case class Def1_CreateAmendPensionChargesRequestBody(pensionSavingsTaxCharges: Option[PensionSavingsTaxCharges],
                                                     pensionSchemeOverseasTransfers: Option[PensionSchemeOverseasTransfers],
                                                     pensionSchemeUnauthorisedPayments: Option[PensionSchemeUnauthorisedPayments],
                                                     pensionContributions: Option[PensionContributions],
                                                     overseasPensionContributions: Option[OverseasPensionContributions])
    extends CreateAmendPensionChargesRequestBody

object Def1_CreateAmendPensionChargesRequestBody {
  implicit val format: OFormat[Def1_CreateAmendPensionChargesRequestBody] = Json.format[Def1_CreateAmendPensionChargesRequestBody]

}
