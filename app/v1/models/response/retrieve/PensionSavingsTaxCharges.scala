/*
 * Copyright 2021 HM Revenue & Customs
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

package v1.models.response.retrieve

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class PensionSavingsTaxCharges(pensionSchemeTaxReference: Seq[String],
                                    lumpSumBenefitTakenInExcessOfLifetimeAllowance: Option[LifetimeAllowance],
                                    benefitInExcessOfLifetimeAllowance: Option[LifetimeAllowance],
                                    isAnnualAllowanceReduced: Boolean,
                                    taperedAnnualAllowance: Option[Boolean],
                                    moneyPurchasedAllowance: Option[Boolean])

object PensionSavingsTaxCharges {

  implicit val writes: Writes[PensionSavingsTaxCharges] = Json.writes[PensionSavingsTaxCharges]
  implicit val reads: Reads[PensionSavingsTaxCharges] = (
    (__ \ "pensionSchemeTaxReference").read[Seq[String]] and
      (__ \ "lumpSumBenefitTakenInExcessOfLifetimeAllowance").readNullable[LifetimeAllowance] and
      (__ \ "benefitInExcessOfLifetimeAllowance").readNullable[LifetimeAllowance] and
      (__ \ "isAnnualAllowanceReduced").read[Boolean] and
      (__ \ "taperedAnnualAllowance").readNullable[Boolean] and
      (__ \ "moneyPurchasedAllowance").readNullable[Boolean]
    ) (PensionSavingsTaxCharges.apply _)

}
