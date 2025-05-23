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

package v3.pensionCharges.createAmend.model.request

import play.api.libs.json.{JsObject, Json, OWrites}
import shared.utils.JsonWritesUtil
import v3.pensionCharges.createAmend.def1.model.request.Def1_CreateAmendPensionChargesRequestBody
import v3.pensionCharges.createAmend.def2.model.request.Def2_CreateAmendPensionChargesRequestBody

trait CreateAmendPensionChargesRequestBody

object CreateAmendPensionChargesRequestBody extends JsonWritesUtil {

  implicit val writes: OWrites[CreateAmendPensionChargesRequestBody] = writesFrom {
    case def1: Def1_CreateAmendPensionChargesRequestBody =>
      Json.toJson(def1).as[JsObject]
    case def2: Def2_CreateAmendPensionChargesRequestBody =>
      Json.toJson(def2).as[JsObject]
  }

}
