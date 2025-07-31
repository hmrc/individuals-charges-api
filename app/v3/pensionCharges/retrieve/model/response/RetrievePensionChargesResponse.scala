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

package v3.pensionCharges.retrieve.model.response

import play.api.libs.json.*
import shared.utils.JsonWritesUtil
import v3.pensionCharges.retrieve.def1.model.response.Def1_RetrievePensionChargesResponse
import v3.pensionCharges.retrieve.def2.model.response.Def2_RetrievePensionChargesResponse

trait RetrievePensionChargesResponse

object RetrievePensionChargesResponse extends JsonWritesUtil {

  implicit val writes: OWrites[RetrievePensionChargesResponse] = writesFrom {
    case def1: Def1_RetrievePensionChargesResponse =>
      implicitly[OWrites[Def1_RetrievePensionChargesResponse]].writes(def1)
    case def2: Def2_RetrievePensionChargesResponse =>
      implicitly[OWrites[Def2_RetrievePensionChargesResponse]].writes(def2)
  }

}
