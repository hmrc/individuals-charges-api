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

package internal.fts.callback.model

case class Property(
    name: String,
    value: String
)

case class Checksum(
    algorithm: String,
    value: String
)

case class File(
    recipientOrSender: Option[String],
    name: String,
    location: Option[String],
    checksum: Checksum,
    size: Int,
    properties: List[Property]
)

case class Audit(
    correlationID: String
)

case class FileTransferNotification(
    informationType: String,
    file: File,
    audit: Audit
)

import play.api.libs.json.*
import play.api.libs.functional.syntax.*
import play.api.libs.json.Reads.{filter, pattern}

object Property {
  implicit val format: OFormat[Property] = Json.format[Property]
}

object Checksum {
  implicit val format: OFormat[Checksum] = Json.format[Checksum]
}

object File {
  implicit val format: OFormat[File] = Json.format[File]
}

object Audit {
  implicit val format: OFormat[Audit] = Json.format[Audit]
}

object FileTransferNotification {

  private val validAlgorithms = Set("md5", "SHA-512", "SHA-256")

  private given Reads[Property] =
    ((__ \ "name").read[String]
      ~ (__ \ "value").read[String])(Property.apply)

  private given Reads[Checksum] =
    ((__ \ "algorithm").read[String](
      filter(JsonValidationError(s"Invalid Json. Parameter algorithm is missing or invalid (one of ${validAlgorithms.mkString(",")})"))(
        validAlgorithms.contains))
      ~ (__ \ "value").read[String])(Checksum.apply)

  private given Reads[Audit] =
    (__ \ "correlationID")
      .read[String](
        pattern(
          "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}".r,
          "Invalid Json. Parameter correlationID is missing or invalid (i.e not a hex 8-4-4-4-12 UUID)"
        )
      )
      .map(Audit.apply)

  private given Reads[File] =
    ((__ \ "recipientOrSender").readNullable[String]
      ~ (__ \ "name").read[String]
      ~ (__ \ "location").readNullable[String]
      ~ (__ \ "checksum").read[Checksum]
      ~ (__ \ "size").read[Int]
      ~ (__ \ "properties").read[List[Property]])(File.apply)

  val fileTransferNotificationReads: Reads[FileTransferNotification] =
    ((__ \ "informationType").read[String]
      ~ (__ \ "file").read[File]
      ~ (__ \ "audit").read[Audit])(FileTransferNotification.apply)

}
