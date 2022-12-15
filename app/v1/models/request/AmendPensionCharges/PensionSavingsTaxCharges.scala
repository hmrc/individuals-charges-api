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
import play.api.libs.functional.syntax._

case class PensionSavingsTaxCharges(pensionSchemeTaxReference: Seq[String],
                                    lumpSumBenefitTakenInExcessOfLifetimeAllowance: Option[LifetimeAllowance],
                                    benefitInExcessOfLifetimeAllowance: Option[LifetimeAllowance])

object PensionSavingsTaxCharges {

  implicit val reads: Reads[PensionSavingsTaxCharges] = Json.reads[PensionSavingsTaxCharges]

  implicit val writes: Writes[PensionSavingsTaxCharges] = (
    (__ \ "pensionSchemeTaxReference").write[Seq[String]] and
      (__ \ "lumpSumBenefitTakenInExcessOfLifetimeAllowance").writeNullable[LifetimeAllowance] and
      (__ \ "benefitInExcessOfLifetimeAllowance").writeNullable[LifetimeAllowance]
  )(unlift(PensionSavingsTaxCharges.unapply))

}
