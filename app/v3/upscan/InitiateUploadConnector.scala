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
import api.connectors.httpparsers.StandardDownstreamHttpParser.readsEmpty
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome, DownstreamStrategy, DownstreamUri}
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import v3.upscan.model.{InitiateUploadRequest, InitiateUploadResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class InitiateUploadConnector @Inject(val http: HttpClientV2, val appConfig: AppConfig) extends BaseDownstreamConnector{

  def initiateUpload(
      request: InitiateUploadRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext, correlationId: String): Future[DownstreamOutcome[InitiateUploadResponse]] = {

    import request.*

    def doPost(): Future[DownstreamOutcome[InitiateUploadResponse]] = {
      http.post(url"http://localhost:9570/upscan/v2/initiate").withBody(Json.toJson(request)).execute
    }

    for {
      result  <- doPost()
    } yield result
  }
}