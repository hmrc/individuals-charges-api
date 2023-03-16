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

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class PensionContributionsSpec extends UnitSpec {

  val modelWithoutCl102Fields: PensionContributions = PensionContributions(Seq("00123456RA", "00123456RA"), 123.12, 123.12, None, None, None)

  val modelWithCl102Fields: PensionContributions =
    PensionContributions(Seq("00123456RA", "00123456RA"), 123.12, 123.12, Some(true), Some(true), Some(false))

  val requestJsonWithCl102Fields: JsValue = Json.parse("""
      |{
      |    "pensionSchemeTaxReference": ["00123456RA", "00123456RA"],
      |    "inExcessOfTheAnnualAllowance": 123.12,
      |    "annualAllowanceTaxPaid": 123.12,
      |    "isAnnualAllowanceReduced": true,
      |    "taperedAnnualAllowance": true,
      |    "moneyPurchasedAllowance": false
      |}""".stripMargin)

  val requestJsonWithoutCl102Fields: JsValue = Json.parse("""
      |{
      |    "pensionSchemeTaxReference": ["00123456RA", "00123456RA"],
      |    "inExcessOfTheAnnualAllowance": 123.12,
      |    "annualAllowanceTaxPaid": 123.12
      |}""".stripMargin)

  "reads" when {
    "passed valid JSON without Cl102 fields" should {
      "return a valid model" in {
        requestJsonWithoutCl102Fields.as[PensionContributions] shouldBe modelWithoutCl102Fields
      }
    }

    "passed valid JSON with Cl102 fields" should {
      "return a valid model ignoring Cl102 fields" in {
        requestJsonWithCl102Fields.as[PensionContributions] shouldBe modelWithoutCl102Fields
      }
    }
  }

  "writes" when {
    "passed valid model without Cl102 fields" should {
      "return valid JSON" in {
        Json.toJson(modelWithoutCl102Fields) shouldBe requestJsonWithoutCl102Fields
      }
    }

    "passed valid model with Cl102 fields" should {
      "return valid JSON including Cl102 fields" in {
        Json.toJson(modelWithCl102Fields) shouldBe requestJsonWithCl102Fields
      }
    }
  }

}
