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

package v1.models.response.retrievePensionCharges

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v1.fixture.RetrievePensionChargesFixture.{pensionSavingsChargeWithCl102Fields, pensionSavingsChargeWithoutCl102Fields}

class PensionSavingsTaxChargesSpec extends UnitSpec {

  val responseModelWithCL102Fields: PensionSavingsTaxCharges = PensionSavingsTaxCharges(
    Seq("00123456RA"),
    Some(LifetimeAllowance(123.12, 123.12)),
    Some(LifetimeAllowance(123.12, 123.12)),
    isAnnualAllowanceReduced = Some(true),
    taperedAnnualAllowance = Some(true),
    moneyPurchasedAllowance = Some(true)
  )

  val responseModelWithoutCL102Fields: PensionSavingsTaxCharges = PensionSavingsTaxCharges(
    Seq("00123456RA"),
    Some(LifetimeAllowance(123.12, 123.12)),
    Some(LifetimeAllowance(123.12, 123.12)),
    None,
    None,
    None
  )

  val responseJsonWithCL102Fields: JsValue = Json.parse("""
      |{
      |      "pensionSchemeTaxReference": ["00123456RA"],
      |      "lumpSumBenefitTakenInExcessOfLifetimeAllowance":
      |         {
      |            "amount":123.12,
      |            "taxPaid":123.12
      |         },
      |      "benefitInExcessOfLifetimeAllowance":
      |         {
      |            "amount":123.12,
      |            "taxPaid":123.12
      |         },
      |       "isAnnualAllowanceReduced": true,
      |       "taperedAnnualAllowance": true,
      |       "moneyPurchasedAllowance": true
      |   }
      |""".stripMargin)

  val responseJsonWithoutCL102Fields: JsValue = Json.parse("""
      |{
      |      "pensionSchemeTaxReference": ["00123456RA"],
      |      "lumpSumBenefitTakenInExcessOfLifetimeAllowance":
      |         {
      |            "amount":123.12,
      |            "taxPaid":123.12
      |         },
      |      "benefitInExcessOfLifetimeAllowance":
      |         {
      |            "amount":123.12,
      |            "taxPaid":123.12
      |         }
      |   }
      |""".stripMargin)

  "reads" when {
    "passed valid JSON with CL102 fields" should {
      "return a valid model" in {
        responseModelWithCL102Fields shouldBe responseJsonWithCL102Fields.as[PensionSavingsTaxCharges]
      }
    }

    "passed valid JSON without CL102 fields" should {
      "return a valid model" in {
        responseModelWithoutCL102Fields shouldBe responseJsonWithoutCL102Fields.as[PensionSavingsTaxCharges]
      }
    }
  }

  "writes" when {
    "passed valid model with CL102 fields" should {
      "return valid JSON" in {
        Json.toJson(responseModelWithCL102Fields) shouldBe responseJsonWithCL102Fields
      }
    }

    "passed valid model without CL102 fields" should {
      "return valid JSON" in {
        Json.toJson(responseModelWithoutCL102Fields) shouldBe responseJsonWithoutCL102Fields
      }
    }
  }

  "isIsAnnualAllowanceReducedMissing" when {
    "missing from pensionSavingsTaxCharges" should {
      "return true" in {
        pensionSavingsChargeWithoutCl102Fields.isIsAnnualAllowanceReducedMissing shouldBe true
      }
    }

    "present in pensionSavingsTaxCharges" should {
      "return false" in {
        pensionSavingsChargeWithCl102Fields.isIsAnnualAllowanceReducedMissing shouldBe false
      }
    }
  }

}
