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

package v3.pensionCharges.createAmend.def1.model.request

import play.api.libs.json.{JsValue, Json}
import shared.utils.UnitSpec
import v3.pensionCharges.createAmend.def1.model.request.PensionContributions

class PensionContributionsSpec extends UnitSpec {

  val requestModel: PensionContributions = PensionContributions(
    pensionSchemeTaxReference = Seq("00123456RA", "00123456RA"),
    inExcessOfTheAnnualAllowance = 123.12,
    annualAllowanceTaxPaid = 123.12,
    isAnnualAllowanceReduced = Some(true),
    taperedAnnualAllowance = Some(true),
    moneyPurchasedAllowance = Some(false)
  )

  val requestJson: JsValue = Json.parse("""
      |{
      |     "pensionSchemeTaxReference": ["00123456RA", "00123456RA"],
      |     "inExcessOfTheAnnualAllowance": 123.12,
      |     "annualAllowanceTaxPaid": 123.12,
      |     "isAnnualAllowanceReduced": true,
      |     "taperedAnnualAllowance": true,
      |     "moneyPurchasedAllowance": false
      |}""".stripMargin)

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        requestModel shouldBe requestJson.as[PensionContributions]
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(requestModel) shouldBe requestJson
      }
    }
  }

}
