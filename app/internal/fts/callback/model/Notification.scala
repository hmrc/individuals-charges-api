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

import play.api.libs.json.*

enum Notification(val asString: String):
  case FileReady             extends Notification("FileReady")
  case FileReceived          extends Notification("FileReceived")
  case FileProcessed         extends Notification("FileProcessed")
  case FileProcessingFailure extends Notification("FileProcessingFailure")

object Notification {

  private val notifications = Seq(
    Notification.FileReady,
    Notification.FileReceived,
    Notification.FileProcessed,
    Notification.FileProcessingFailure
  )

  def parse(s: String): Option[Notification] = notifications.find(_.asString == s)

  implicit val format: Format[Notification] = Format(
    Reads {
      case JsString(asString) =>
        parse(asString) match
          case Some(notification) => JsSuccess(notification)
          case None =>
            JsError(Seq(JsPath -> Seq(JsonValidationError(s"Unknown notification: $asString"))))
      case _ =>
        JsError(Seq(JsPath -> Seq(JsonValidationError("Expected a notification string"))))
    },
    Writes(notification => JsString(notification.asString))
  )

}
