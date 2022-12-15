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

package v1.models.request.AmendPensionCharges

import play.api.libs.json._

case class PensionContributions(pensionSchemeTaxReference: Seq[String],
                                isAnnualAllowanceReduced: Option[Boolean],
                                taperedAnnualAllowance: Option[Boolean],
                                moneyPurchasedAllowance: Option[Boolean],
                                inExcessOfTheAnnualAllowance: BigDecimal,
                                annualAllowanceTaxPaid: BigDecimal)

object PensionContributions {

  implicit val reads: Reads[PensionContributions] = for {
    pensionSchemeTaxReference    <- (__ \ "pensionSchemeTaxReference").read[Seq[String]]
    isAnnualAllowanceReduced    <- (__ \ "isAnnualAllowanceReduced").readNullable[Boolean]
    taperedAnnualAllowance      <- (__ \ "taperedAnnualAllowance").readNullable[Boolean]
    moneyPurchasedAllowance     <- (__ \ "moneyPurchasedAllowance").readNullable[Boolean]
    inExcessOfTheAnnualAllowance <- (__ \ "inExcessOfTheAnnualAllowance").read[BigDecimal]
    annualAllowancePaid          <- (__ \ "annualAllowanceTaxPaid").read[BigDecimal]
  } yield {
    PensionContributions(
      pensionSchemeTaxReference = pensionSchemeTaxReference,
      isAnnualAllowanceReduced = isAnnualAllowanceReduced,
      taperedAnnualAllowance = taperedAnnualAllowance,
      moneyPurchasedAllowance = moneyPurchasedAllowance,
      inExcessOfTheAnnualAllowance = inExcessOfTheAnnualAllowance,
      annualAllowanceTaxPaid = annualAllowancePaid
    )
  }

  implicit val writes: Writes[PensionContributions] = (p: PensionContributions) => {
    JsObject(
      Map(
        "pensionSchemeTaxReference"    -> Json.toJson(p.pensionSchemeTaxReference),
        "isAnnualAllowanceReduced"     -> Json.toJson(p.isAnnualAllowanceReduced),
        "taperedAnnualAllowance"       -> Json.toJson(p.taperedAnnualAllowance),
        "moneyPurchasedAllowance"      -> Json.toJson(p.moneyPurchasedAllowance),
        "inExcessOfTheAnnualAllowance" -> Json.toJson(p.inExcessOfTheAnnualAllowance),
        "annualAllowanceTaxPaid"       -> Json.toJson(p.annualAllowanceTaxPaid)
      ))
  }

}
