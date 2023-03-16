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

package v1.models.request.AmendPensionCharges

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{Json, Reads, Writes, __}

case class PensionContributions(pensionSchemeTaxReference: Seq[String],
                                inExcessOfTheAnnualAllowance: BigDecimal,
                                annualAllowanceTaxPaid: BigDecimal,
                                isAnnualAllowanceReduced: Option[Boolean],
                                taperedAnnualAllowance: Option[Boolean],
                                moneyPurchasedAllowance: Option[Boolean])

object PensionContributions {

  implicit val writes: Writes[PensionContributions] = Json.writes[PensionContributions]

  implicit val reads: Reads[PensionContributions] = (
    (__ \ "pensionSchemeTaxReference").read[Seq[String]] and
      (__ \ "inExcessOfTheAnnualAllowance").read[BigDecimal] and
      (__ \ "annualAllowanceTaxPaid").read[BigDecimal] and
      Reads.pure(None) and // Ensure these fields are not read from the vendor request
      Reads.pure(None) and
      Reads.pure(None)
  )(PensionContributions.apply _)

}
