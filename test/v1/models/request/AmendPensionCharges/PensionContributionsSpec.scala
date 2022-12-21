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

import play.api.libs.json.{Json, JsValue}
import support.UnitSpec

class PensionContributionsSpec extends UnitSpec {

  val requestModel = PensionContributions(Seq("00123456RA", "00123456RA"), 123.12, 123.12, Option(true), Option(false), Option(true))
  val responseModel: PensionContributions = PensionContributions(Seq("00123456RA", "00123456RA"), 123.12, 123.12, Some(true), Some(true), Some(true))

  val requestJson = Json.parse("""
      |{
      |     "pensionSchemeTaxReference": ["00123456RA", "00123456RA"],
      |     "inExcessOfTheAnnualAllowance": 123.12,
      |     "annualAllowanceTaxPaid": 123.12,
      |      "isAnnualAllowanceReduced": true,
      |      "taperedAnnualAllowance": false,
      |      "moneyPurchasedAllowance": true
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
