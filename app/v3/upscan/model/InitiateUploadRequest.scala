package v3.upscan.model

import play.api.libs.json.{Json, OFormat, Reads, Writes}

case class InitiateUploadRequest(callbackUrl: String,
                                 successRedirect: Option[String],
                                 errorRedirect: Option[String],
                                 minimumFileSize: Option[Long],
                                 maximumFileSize: Option[Long],
                                 consumingService: Option[String])

object InitiateUploadRequest {

  implicit val initiateUploadRequestFormat: OFormat[InitiateUploadRequest] =
    Json.format[InitiateUploadRequest]

}
