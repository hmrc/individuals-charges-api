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

package internal.upscan.callback.model

import play.api.libs.json.{JsError, JsSuccess, Reads, __}

trait UpscanCallbackRequestBody {
  val reference: String
}

object UpscanCallbackRequestBody {

  implicit val reads: Reads[UpscanCallbackRequestBody] = { json =>
    (__ \ "fileStatus").read[String].reads(json) match {
      case JsSuccess("READY", _)  => json.validate[UpscanCallbackRequestBodySuccess]
      case JsSuccess("FAILED", _) => json.validate[UpscanCallbackRequestBodyFailure]
      case JsSuccess(other, _)    => JsError(__ \ "fileStatus", s"Unknown fileStatus: $other")
      case e: JsError             => e
    }
  }

}
