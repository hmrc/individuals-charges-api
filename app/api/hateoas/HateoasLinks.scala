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

package api.hateoas

import api.hateoas.Method._
import api.hateoas.RelType._
import config.AppConfig

trait HateoasLinks {
  // Individuals Charges API Domain URIs

  private def retrieveBaseUrl(appConfig: AppConfig, nino: String, taxYear: String): String =
    s"/${appConfig.apiGatewayContext}/pensions/$nino/$taxYear"

  private def deleteBaseUrl(appConfig: AppConfig, nino: String, taxYear: String): String =
    s"/${appConfig.apiGatewayContext}/pensions/$nino/$taxYear"

  private def amendBaseUrl(appConfig: AppConfig, nino: String, taxYear: String): String =
    s"/${appConfig.apiGatewayContext}/pensions/$nino/$taxYear"

  // Individual Charges API resource links

  def getRetrievePensions(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(href = retrieveBaseUrl(appConfig, nino, taxYear), method = GET, rel = SELF)

  def getDeletePensions(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(href = deleteBaseUrl(appConfig, nino, taxYear), method = DELETE, rel = DELETE_PENSION_CHARGES)

  def getAmendPensions(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(href = amendBaseUrl(appConfig, nino, taxYear), method = PUT, rel = AMEND_PENSION_CHARGES)

}
