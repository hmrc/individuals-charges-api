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

package v1.models.response.retrievePensionCharges

import anyVersion.models.response.retrievePensionCharges.{OverseasPensionContributions, PensionSchemeOverseasTransfers}
import api.hateoas.{HateoasLinks, HateoasLinksFactory}
import api.models.hateoas.{HateoasData, Link}
import config.AppConfig
import play.api.libs.json._

case class RetrievePensionChargesResponse(pensionSavingsTaxCharges: Option[PensionSavingsTaxCharges],
                                          pensionSchemeOverseasTransfers: Option[PensionSchemeOverseasTransfers],
                                          pensionSchemeUnauthorisedPayments: Option[PensionSchemeUnauthorisedPayments],
                                          pensionContributions: Option[PensionContributions],
                                          overseasPensionContributions: Option[OverseasPensionContributions]) {

  def removeFieldsFromPensionSavingsTaxCharges: RetrievePensionChargesResponse = {

    val updatedPensionSavingsTaxCharges = this.pensionSavingsTaxCharges
      .map(_.copy(isAnnualAllowanceReduced = None, taperedAnnualAllowance = None, moneyPurchasedAllowance = None))
      .filter(_.isDefined)
    copy(pensionSavingsTaxCharges = updatedPensionSavingsTaxCharges)

  }

  def removeFieldsFromPensionContributions: RetrievePensionChargesResponse = {

    val updatedPensionContributions = this.pensionContributions
      .map(_.copy(isAnnualAllowanceReduced = None, taperedAnnualAllowance = None, moneyPurchasedAllowance = None))
      .filter(_.isDefined)
    copy(pensionContributions = updatedPensionContributions)

  }

  def addFieldsFromPensionContributionsToPensionSavingsTaxCharges: RetrievePensionChargesResponse = {
    val updatedPensionSavingsTaxCharges = this.pensionSavingsTaxCharges
      .map(
        _.copy(
          isAnnualAllowanceReduced = this.pensionContributions.flatMap(_.isAnnualAllowanceReduced),
          taperedAnnualAllowance = this.pensionContributions.flatMap(_.taperedAnnualAllowance),
          moneyPurchasedAllowance = this.pensionContributions.flatMap(_.moneyPurchasedAllowance)
        ))
    copy(pensionSavingsTaxCharges = updatedPensionSavingsTaxCharges)
  }

}

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
