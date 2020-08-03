/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.connectors.httpparsers

import play.api.Logger
import play.api.http.Status._
import play.api.libs.json.Reads
import uk.gov.hmrc.http.{ HttpReads, HttpResponse }
import v1.connectors.DesOutcome
import v1.models.errors.{ DownstreamError, OutboundError }
import v1.models.outcomes.DesResponse

object StandardDesHttpParser extends HttpParser {

  val logger = Logger(getClass)

  // Return Right[DesResponse[Unit]] as success response has no body - no need to assign it a value
  implicit val readsEmpty: HttpReads[DesOutcome[Unit]] = (_: String, url: String, response: HttpResponse) => {
    doRead(NO_CONTENT, url, response)(correlationId => Right(DesResponse(correlationId, ())))
  }

  implicit def reads[A: Reads]: HttpReads[DesOutcome[A]] = (_: String, url: String, response: HttpResponse) => {
    doRead(OK, url, response) { correlationId =>
      response.validateJson[A] match {
        case Some(ref) => Right(DesResponse(correlationId, ref))
        case None => Left(DesResponse(correlationId, OutboundError(DownstreamError)))
      }
    }
  }

  private def doRead[A](successStatusCode: Int, url: String, response: HttpResponse)(successOutcomeFactory: String => DesOutcome[A]): DesOutcome[A] = {

    import utils.PagerDutyHelper.pagerDutyLog
    import utils.PagerDutyHelper.PagerDutyKeys._

    val correlationId = retrieveCorrelationId(response)

    val log = s"[StandardDesHttpParser][read] - MtdError response received from DES with status: ${response.status} and body\n" +
      s"${response.body} and correlationId: $correlationId when calling $url"

    response.status match {
      case `successStatusCode` =>
        logger.info(s"[StandardDesHttpParser][read] - Success response received from DES with correlationId: $correlationId when calling $url")
        successOutcomeFactory(correlationId)

      case BAD_REQUEST | NOT_FOUND | FORBIDDEN | CONFLICT | UNPROCESSABLE_ENTITY =>
        logger.warn(log)
        Left(DesResponse(correlationId, parseErrors(response)))

      case INTERNAL_SERVER_ERROR =>
        pagerDutyLog(DES_INTERNAL_SERVER_ERROR,Some(log))
        Left(DesResponse(correlationId, OutboundError(DownstreamError)))

      case SERVICE_UNAVAILABLE =>
        pagerDutyLog(DES_SERVICE_UNAVAILABLE,Some(log))
        Left(DesResponse(correlationId, OutboundError(DownstreamError)))

      case _ =>
        pagerDutyLog(DES_UNEXPECTED_RESPONSE,Some(log))
        Left(DesResponse(correlationId, OutboundError(DownstreamError)))
    }
  }
}
