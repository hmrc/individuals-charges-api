/*
 * Copyright 2022 HM Revenue & Customs
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

package v1.connectors

import config.AppConfig

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpClient
import v1.connectors.DownstreamUri.{DesUri, IfsUri, TaxYearSpecificIfsUri}
import v1.connectors.httpparsers.StandardDownstreamHttpParser._
import v1.models.request.AmendPensionCharges.AmendPensionChargesRequest
import v1.models.request.DeletePensionCharges.DeletePensionChargesRequest
import v1.models.request.RetrievePensionCharges.RetrievePensionChargesRequest
import v1.models.response.retrieve.RetrievePensionChargesResponse

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PensionChargesConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def deletePensionCharges(request: DeletePensionChargesRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Unit]] = {

    val nino    = request.nino.nino
    val taxYear = request.taxYear.asMtd

    val downstreamUri = IfsUri[Unit](s"income-tax/charges/pensions/$nino/$taxYear")
    delete(downstreamUri)
  }

  def retrievePensionCharges(request: RetrievePensionChargesRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[RetrievePensionChargesResponse]] = {

    val nino    = request.nino.nino
    val taxYear = request.taxYear

    val downstreamUri =
      if (request.taxYear.useTaxYearSpecificApi) {
        TaxYearSpecificIfsUri[RetrievePensionChargesResponse](s"income-tax/charges/pensions/${taxYear.asTysDownstream}/$nino")
      } else {
        DesUri[RetrievePensionChargesResponse](s"income-tax/charges/pensions/$nino/${taxYear.asMtd}")
      }
    get(downstreamUri)
  }

  def amendPensionCharges(request: AmendPensionChargesRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Unit]] = {

    val nino    = request.nino.nino
    val taxYear = request.taxYear

    val downstreamUri = if(request.taxYear.useTaxYearSpecificApi) {
      TaxYearSpecificIfsUri[Unit](s"income-tax/charges/pensions/${taxYear.asTysDownstream}/$nino")
    } else {
      IfsUri[Unit](s"income-tax/charges/pensions/$nino/${taxYear.asMtd}")
    }

    put(
      uri = downstreamUri,
      body = request.pensionCharges
    )
  }

}
