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

package internal.fts.callback

import api.config.AppConfig
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import api.models.errors.{MtdError, OutboundError}
import api.models.outcomes.ResponseWrapper
import internal.fts.callback.model.*
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FTSCallbackConnector @Inject(val http: HttpClientV2, val appConfig: AppConfig) extends BaseDownstreamConnector{

  def handleCallback(
      request: FTSCallbackRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext, correlationId: String): Future[DownstreamOutcome[Unit]] = {

    // TODO: Define the downstream URI fo AMS update
//    val downstreamUri: DownstreamUri[Unit] = HipUri(s"itsd/attachment-metadata/${request.body.reference}")

    request.body.notification match {
      case Notification.FileReady             => handleSuccessCallback(correlationId)
      case Notification.FileReceived          => handleSuccessCallback(correlationId)
      case Notification.FileProcessed         => handleSuccessCallback(correlationId)
      case Notification.FileProcessingFailure => handleFailureCallback(correlationId)
//      case _                                  => handleFailureCallback()
    }
  }

  private def handleSuccessCallback(correlationId: String): Future[DownstreamOutcome[Unit]] = {
    // TODO: AMS update
    Future.successful(Right(ResponseWrapper(correlationId, ())))
  }

  private def handleFailureCallback(correlationId: String): Future[DownstreamOutcome[Unit]] = {
    // TODO: AMS update
    Future.successful(Left(ResponseWrapper(correlationId, OutboundError(MtdError(code = "code", message = "message", httpStatus = 500)))))
  }
}
