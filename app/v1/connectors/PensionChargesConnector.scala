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

package v1.connectors

import config.AppConfig
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import v1.connectors.httpparsers.StandardDesHttpParser._
import v1.models.des.RetrievePensionChargesResponse
import v1.models.requestData._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PensionChargesConnector @Inject()(val http: HttpClient,
                                        val appConfig: AppConfig) extends DesConnector {

  def deletePensionCharges(request: DeletePensionChargesRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[DesOutcome[Unit]] = {
    val nino = request.nino.nino
    val taxYear = request.taxYear.value

    http.DELETE[DesOutcome[Unit]](s"${appConfig.desBaseUrl}/income-tax/charges/pensions/$nino/$taxYear")(readsEmpty,desHeaderCarrier(appConfig),ec)
  }

  def retrievePensionCharges(request: RetrievePensionChargesRequest)
                            (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[DesOutcome[RetrievePensionChargesResponse]] = {
    val nino = request.nino.nino
    val taxYear = request.taxYear.value

    def doIt(implicit hc: HeaderCarrier): Future[DesOutcome[RetrievePensionChargesResponse]] = {
      http.GET[DesOutcome[RetrievePensionChargesResponse]](s"${appConfig.desBaseUrl}/income-tax/charges/pensions/$nino/$taxYear")
    }

    doIt(desHeaderCarrier(appConfig))
  }

  def amendPensionCharges(request: AmendPensionChargesRequest)(implicit hc: HeaderCarrier,
                                                          ec: ExecutionContext): Future[DesOutcome[Unit]] = {

    val nino = request.nino.nino
    val taxYear = request.taxYear.value

    def doIt(implicit hc: HeaderCarrier) =
      http.PUT[PensionCharges, DesOutcome[Unit]](s"${appConfig.desBaseUrl}/income-tax/charges/pensions/$nino/$taxYear", request.pensionCharges)

    doIt(desHeaderCarrier(appConfig))
  }
}
