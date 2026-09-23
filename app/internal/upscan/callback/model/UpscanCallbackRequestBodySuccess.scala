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

import play.api.libs.json.{Json, OFormat, Reads, Writes}

case class UploadDetails(
    fileName: String,
    fileMimeType: String,
    uploadTimestamp: String,
    checksum: String,
    size: Int
)

object UploadDetails {

  implicit val uploadDetailsFormat: OFormat[UploadDetails] =
    Json.format[UploadDetails]

}

case class UpscanCallbackRequestBodySuccess(
    reference: String,
    downloadUrl: String,
    fileStatus: String,
    uploadDetails: UploadDetails
) extends UpscanCallbackRequestBody

object UpscanCallbackRequestBodySuccess {

  implicit val upscanCallbackRequestBodySuccessFormat: OFormat[UpscanCallbackRequestBodySuccess] =
    Json.format[UpscanCallbackRequestBodySuccess]

}
