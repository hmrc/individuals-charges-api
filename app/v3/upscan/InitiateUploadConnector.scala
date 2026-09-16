/*
 * Copyright 2023 HM Revenue & Customs
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

package v3.upscan

import api.config.AppConfig
import api.connectors.DownstreamUri.HipUri
import api.connectors.httpparsers.StandardDownstreamHttpParser.*
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome, DownstreamUri}
import cats.data.EitherT
import play.api.libs.json.Json
import play.api.libs.ws.writeableOf_JsValue
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import v3.upscan.model.{AMSCreateBody, InitiateUploadRequestBody, InitiateUploadRequestData, InitiateUploadResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class InitiateUploadConnector @Inject(val http: HttpClientV2, val appConfig: AppConfig) extends BaseDownstreamConnector{

  def initiateUpload(
      request: InitiateUploadRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext, correlationId: String): Future[DownstreamOutcome[InitiateUploadResponse]] = {

    def doPost(): Future[DownstreamOutcome[InitiateUploadResponse]] = {
      val initiateUploadRequestBody = InitiateUploadRequestBody(callbackUrl = "https://myservice.com/callback",
        successRedirect = request.body.successRedirect,
        errorRedirect = request.body.errorRedirect,
        minimumFileSize = Some(0),
        maximumFileSize = Some(1024),
        consumingService = None)

      http.post(url"http://localhost:9570/upscan/v2/initiate").withBody(Json.toJson(initiateUploadRequestBody)).execute
    }

    def doSecondPost(reference: String): Future[DownstreamOutcome[Unit]] = {
      val downstreamUri: DownstreamUri[Unit] =
        HipUri(s"itsd/attachment-metadata/${request.nino}/$reference?taxYear=${request.taxYear.asTysDownstream}")

      val amsCreateBody = AMSCreateBody("Agent", "Requested", "2026-04-30T16:05:42Z")
      post(body = amsCreateBody, uri = downstreamUri)
    }

    val eitherTResult = for {
      result  <- EitherT(doPost())
      _ <- EitherT(doSecondPost(result.responseData.reference))
    } yield result

    eitherTResult.value
  }
}
