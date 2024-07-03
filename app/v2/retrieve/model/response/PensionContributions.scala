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

package v2.retrieve.model.response

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class PensionContributions(pensionSchemeTaxReference: Seq[String],
                                isAnnualAllowanceReduced: Option[Boolean],
                                taperedAnnualAllowance: Option[Boolean],
                                moneyPurchasedAllowance: Option[Boolean],
                                inExcessOfTheAnnualAllowance: BigDecimal,
                                annualAllowanceTaxPaid: BigDecimal)

object PensionContributions {
  implicit val format: OFormat[PensionContributions] = Json.format[PensionContributions]

  implicit val writes: Writes[PensionContributions] = Json.writes[PensionContributions]

  implicit val reads: Reads[PensionContributions] = (
    (__ \ "pensionSchemeTaxReference").read[Seq[String]] and
      (__ \ "isAnnualAllowanceReduced").readNullable[Boolean] and
      (__ \ "taperedAnnualAllowance").readNullable[Boolean] and
      (__ \ "moneyPurchasedAllowance").readNullable[Boolean] and
      (__ \ "inExcessOfTheAnnualAllowance").read[BigDecimal] and
      (__ \ "annualAllowanceTaxPaid").read[BigDecimal]
  )(PensionContributions.apply _)

}
