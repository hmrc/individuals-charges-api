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

package v3.winterFuelPayment.retrieve

import api.config.AppConfig
import api.connectors.DownstreamUri.HipUri
import api.connectors.httpparsers.StandardDownstreamHttpParser.*
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome, DownstreamUri}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.HeaderCarrier
import v3.winterFuelPayment.retrieve.model.request.RetrieveWinterFuelPaymentRequestData
import v3.winterFuelPayment.retrieve.model.response.RetrieveWinterFuelPaymentResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveWinterFuelPaymentConnector @Inject() (val http: HttpClientV2, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def retrieve(request: RetrieveWinterFuelPaymentRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[RetrieveWinterFuelPaymentResponse]] = {

    import request.*

    val queryParams = Seq("taxYear" -> taxYear.asTysDownstream, "view" -> source.toDownstreamViewString)

    val downstreamUri: DownstreamUri[RetrieveWinterFuelPaymentResponse] =
      HipUri[RetrieveWinterFuelPaymentResponse](s"itsd/charges/winter-fuel-payment/$nino")

    get(downstreamUri, queryParams)
  }

}
