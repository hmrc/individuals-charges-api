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

package v2.models.response.retrievePensionCharges

import anyVersion.models.response.retrievePensionCharges._
import api.hateoas.{HateoasData, HateoasLinks, HateoasLinksFactory, Link}
import config.AppConfig
import play.api.libs.json._

case class RetrievePensionChargesResponse(submittedOn: String,
                                          pensionSavingsTaxCharges: Option[PensionSavingsTaxCharges],
                                          pensionSchemeOverseasTransfers: Option[PensionSchemeOverseasTransfers],
                                          pensionSchemeUnauthorisedPayments: Option[PensionSchemeUnauthorisedPayments],
                                          pensionContributions: Option[PensionContributions],
                                          overseasPensionContributions: Option[OverseasPensionContributions]) {}

object RetrievePensionChargesResponse extends HateoasLinks {
  implicit val format: OFormat[RetrievePensionChargesResponse] = Json.format[RetrievePensionChargesResponse]

  implicit object RetrievePensionChargesLinksFactory extends HateoasLinksFactory[RetrievePensionChargesResponse, RetrievePensionChargesHateoasData] {

    override def links(appConfig: AppConfig, data: RetrievePensionChargesHateoasData): Seq[Link] = {
      import data._
      Seq(
        getRetrievePensions(appConfig, nino, taxYear),
        getAmendPensions(appConfig, nino, taxYear),
        getDeletePensions(appConfig, nino, taxYear)
      )
    }

  }

}

case class RetrievePensionChargesHateoasData(nino: String, taxYear: String) extends HateoasData
