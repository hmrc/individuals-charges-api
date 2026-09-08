package v3.upscan.model

import play.api.libs.json.{Json, OFormat}

case class InitiateUploadResponse(
    reference: String,
    uploadRequest: UploadRequest
)

object InitiateUploadResponse {

  implicit val initiateUploadResponseFormat: OFormat[InitiateUploadResponse] =
    Json.format[InitiateUploadResponse]

}
