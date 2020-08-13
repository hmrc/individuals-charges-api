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

package v1.models.des

import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, OFormat, Reads, Writes, __, _}

case class LifetimeAllowance(amount: BigDecimal, taxPaid: BigDecimal)

object LifetimeAllowance {
  implicit val format: OFormat[LifetimeAllowance] = Json.format[LifetimeAllowance]
}

case class PensionSavingsTaxCharges(pensionSchemeTaxReference: Seq[String],
                                    lumpSumBenefitTakenInExcessOfLifetimeAllowance: Option[LifetimeAllowance],
                                    benefitInExcessOfLifetimeAllowance: Option[LifetimeAllowance],
                                    isAnnualAllowanceReduced: Boolean,
                                    taperedAnnualAllowance: Boolean,
                                    moneyPurchasedAllowance: Boolean)

object PensionSavingsTaxCharges {

  implicit val writes: Writes[PensionSavingsTaxCharges] = Json.writes[PensionSavingsTaxCharges]
  implicit val reads: Reads[PensionSavingsTaxCharges] = (
    (__ \ "pensionSchemeTaxReference").read[Seq[String]] and
      (__ \ "lumpSumBenefitTakenInExcessOfLifetimeAllowance").readNullable[LifetimeAllowance] and
      (__ \ "benefitInExcessOfLifetimeAllowance").readNullable[LifetimeAllowance] and
      (__ \ "isAnnualAllowanceReduced").readNullable[Boolean].map(a => a.getOrElse(false)) and
      (__ \ "taperedAnnualAllowance").readNullable[Boolean].map(a => a.getOrElse(false)) and
      (__ \ "moneyPurchasedAllowance").readNullable[Boolean].map(a => a.getOrElse(false))
    ) (PensionSavingsTaxCharges.apply _)

  implicit val format: Format[PensionSavingsTaxCharges] = Format(reads, writes)
}

case class OverseasSchemeProvider(providerName: String,
                                  providerAddress: String,
                                  providerCountryCode: String,
                                  qualifyingRecognisedOverseasPensionScheme: Option[Seq[String]],
                                  pensionSchemeTaxReference: Option[Seq[String]]
                                 )

object OverseasSchemeProvider {
  implicit val format: OFormat[OverseasSchemeProvider] = Json.format[OverseasSchemeProvider]
}

case class PensionSchemeOverseasTransfers(overseasSchemeProvider: Seq[OverseasSchemeProvider],
                                          transferCharge: BigDecimal,
                                          transferChargeTaxPaid: BigDecimal)

object PensionSchemeOverseasTransfers {
  implicit val format: OFormat[PensionSchemeOverseasTransfers] = Json.format[PensionSchemeOverseasTransfers]
}

case class Charge(amount: BigDecimal, foreignTaxPaid: BigDecimal)

object Charge {
  implicit val format: OFormat[Charge] = Json.format[Charge]
}

case class PensionSchemeUnauthorisedPayments(pensionSchemeTaxReference: Seq[String],
                                             surcharge: Option[Charge],
                                             noSurcharge: Option[Charge])

object PensionSchemeUnauthorisedPayments {
  implicit val format: OFormat[PensionSchemeUnauthorisedPayments] = Json.format[PensionSchemeUnauthorisedPayments]
}

case class PensionContributions(pensionSchemeTaxReference: Seq[String],
                                inExcessOfTheAnnualAllowance: BigDecimal,
                                annualAllowanceTaxPaid: BigDecimal)

object PensionContributions {
  implicit val format: OFormat[PensionContributions] = Json.format[PensionContributions]
}

case class OverseasPensionContributions(overseasSchemeProvider: Seq[OverseasSchemeProvider],
                                        shortServiceRefund: BigDecimal,
                                        shortServiceRefundTaxPaid: BigDecimal)

object OverseasPensionContributions {
  implicit val format: OFormat[OverseasPensionContributions] = Json.format[OverseasPensionContributions]
}

case class RetrievePensionChargesResponse(pensionSavingsTaxCharges: Option[PensionSavingsTaxCharges],
                                          pensionSchemeOverseasTransfers: Option[PensionSchemeOverseasTransfers],
                                          pensionSchemeUnauthorisedPayments: Option[PensionSchemeUnauthorisedPayments],
                                          pensionContributions: Option[PensionContributions],
                                          overseasPensionContributions: Option[OverseasPensionContributions]) {

}

object RetrievePensionChargesResponse {
  implicit val format: OFormat[RetrievePensionChargesResponse] = Json.format[RetrievePensionChargesResponse]
}
