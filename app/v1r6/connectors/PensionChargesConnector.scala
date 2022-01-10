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

package v1r6.connectors

import config.AppConfig
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpClient
import v1r6.connectors.httpparsers.StandardDesHttpParser._
import v1r6.models.request.AmendPensionCharges.{AmendPensionChargesRequest, PensionCharges}
import v1r6.models.request.DeletePensionCharges.DeletePensionChargesRequest
import v1r6.models.request.RetrievePensionCharges.RetrievePensionChargesRequest
import v1r6.models.response.retrieve.RetrievePensionChargesResponse

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PensionChargesConnector @Inject()(val http: HttpClient,
                                        val appConfig: AppConfig) extends BaseDownstreamConnector {

  def deletePensionCharges(request: DeletePensionChargesRequest)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext, correlationId: String): Future[DesOutcome[Unit]] = {

    val nino = request.nino.nino
    val taxYear = request.taxYear.value

    http.DELETE[DesOutcome[Unit]](s"${appConfig.desBaseUrl}/income-tax/charges/pensions/$nino/$taxYear")(readsEmpty,desHeaderCarrier(),ec)
  }

  def retrievePensionCharges(request: RetrievePensionChargesRequest)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext, correlationId: String): Future[DesOutcome[RetrievePensionChargesResponse]] = {

    val nino = request.nino.nino
    val taxYear = request.taxYear.value

    def doIt(implicit hc: HeaderCarrier): Future[DesOutcome[RetrievePensionChargesResponse]] = {
      http.GET[DesOutcome[RetrievePensionChargesResponse]](s"${appConfig.desBaseUrl}/income-tax/charges/pensions/$nino/$taxYear")
    }

    doIt(desHeaderCarrier())
  }

  def amendPensionCharges(request: AmendPensionChargesRequest)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext, correlationId: String): Future[DesOutcome[Unit]] = {

    val nino = request.nino.nino
    val taxYear = request.taxYear.value

    def doIt(implicit hc: HeaderCarrier): Future[DesOutcome[Unit]] =
      http.PUT[PensionCharges, DesOutcome[Unit]](s"${appConfig.ifsBaseUrl}/income-tax/charges/pensions/$nino/$taxYear", request.pensionCharges)

    doIt(ifsHeaderCarrier())
  }
}
