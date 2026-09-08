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

package v3.upscan.models.response

import play.api.libs.json.*

case class Fields(acl: String,
                  key: String,
                  policy: String,
                  `x-amz-algorithm`: String,
                  `x-amz-credential`: String,
                  `x-amz-date`: String,
                  `x-amz-meta-callback-url`: String,
                  `x-amz-signature`: String,
                  success_action_redirect: Option[String],
                  error_action_redirect: Option[String])

object Fields {
  implicit val format: OFormat[Fields] = Json.format[Fields]
}

case class UploadRequest(href: String, fields: Fields)

object UploadRequest {
  implicit val format: OFormat[UploadRequest] = Json.format[UploadRequest]
}

case class UpscanInitiateResponse(reference: String, uploadRequest: UploadRequest)

object UpscanInitiateResponse {

  implicit val format: OFormat[UpscanInitiateResponse] = Json.format[UpscanInitiateResponse]
}
