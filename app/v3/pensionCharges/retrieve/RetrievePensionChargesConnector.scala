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

package v3.pensionCharges.retrieve

import shared.config.SharedAppConfig
import shared.connectors.DownstreamUri.IfsUri
import shared.connectors.httpparsers.StandardDownstreamHttpParser._
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome, DownstreamUri}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.HeaderCarrier
import v3.pensionCharges.retrieve.model.request.RetrievePensionChargesRequestData
import v3.pensionCharges.retrieve.model.response.RetrievePensionChargesResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrievePensionChargesConnector @Inject() (val http: HttpClientV2, val appConfig: SharedAppConfig) extends BaseDownstreamConnector {

  def retrievePensionCharges(request: RetrievePensionChargesRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[RetrievePensionChargesResponse]] = {

    import request._
    import schema._

    val downstreamUri: DownstreamUri[DownstreamResp] = taxYear match {
      case ty if ty.useTaxYearSpecificApi =>
        IfsUri(s"income-tax/charges/pensions/${taxYear.asTysDownstream}/$nino")
      case _ =>
        IfsUri(s"income-tax/charges/pensions/$nino/${taxYear.asMtd}")
    }
    get(downstreamUri)
  }

}
