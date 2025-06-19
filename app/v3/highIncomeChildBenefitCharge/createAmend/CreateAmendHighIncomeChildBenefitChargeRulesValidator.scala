/*
 * Copyright 2025 HM Revenue & Customs
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

package v3.highIncomeChildBenefitCharge.createAmend

import cats.data.Validated
import cats.data.Validated.Invalid
import shared.controllers.validators.RulesValidator
import shared.controllers.validators.resolvers.{ResolveInteger, ResolveIsoDate, ResolveParsedNumber}
import shared.models.domain.TaxYear
import shared.models.errors._
import v3.highIncomeChildBenefitCharge.createAmend.models.request.CreateAmendHighIncomeChildBenefitChargeRequest

object CreateAmendHighIncomeChildBenefitChargeRulesValidator extends RulesValidator[CreateAmendHighIncomeChildBenefitChargeRequest] {
  private val minNumberOfChildren: Int = 1
  private val maxNumberOfChildren: Int = 99

  override def validateBusinessRules(
      parsed: CreateAmendHighIncomeChildBenefitChargeRequest): Validated[Seq[MtdError], CreateAmendHighIncomeChildBenefitChargeRequest] = {
    import parsed.body._
    combine(
      validateNumericFields(numberOfChildren, amountOfChildBenefitReceived),
      validateDateCeased(parsed.taxYear, dateCeased)
    ).onSuccess(parsed)
  }

  private def validateNumericFields(numberOfChildren: Int, amountOfChildBenefitReceived: BigDecimal): Validated[Seq[MtdError], Unit] = {

    val integerValueFormatError: MtdError =
      ValueFormatError.forIntegerPathAndRange("/numberOfChildren", minNumberOfChildren.toString, maxNumberOfChildren.toString)
    combine(
      ResolveInteger(minNumberOfChildren, maxNumberOfChildren)(numberOfChildren, integerValueFormatError),
      ResolveParsedNumber()(amountOfChildBenefitReceived, "/amountOfChildBenefitReceived")
    )
  }

  private def validateDateCeased(taxYear: TaxYear, dateCeased: Option[String]): Validated[Seq[MtdError], Unit] =
    dateCeased.fold(valid) { date =>
      ResolveIsoDate(date, DateCeasedFormatError).andThen { parsedDate =>
        if (parsedDate.isBefore(taxYear.startDate) || parsedDate.isAfter(taxYear.endDate)) {
          Invalid(List(RuleDateCeasedError))
        } else {
          valid
        }
      }
    }

}
