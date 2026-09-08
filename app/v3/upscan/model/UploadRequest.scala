package v3.upscan.model

import play.api.libs.json.{Json, OFormat}

case class UploadRequest(
    href: String,
    fields: Map[String, String]
)

object UploadRequest {

  implicit val uploadRequestFormat: OFormat[UploadRequest] =
    Json.format[UploadRequest]

}
