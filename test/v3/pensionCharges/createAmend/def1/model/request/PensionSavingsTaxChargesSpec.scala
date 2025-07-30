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

class PensionSavingsTaxChargesSpec extends UnitSpec {

  val responseModel: PensionSavingsTaxCharges = PensionSavingsTaxCharges(
    pensionSchemeTaxReference = Seq("00123456RA"),
    lumpSumBenefitTakenInExcessOfLifetimeAllowance = Some(LifetimeAllowance(123.12, 123.12)),
    benefitInExcessOfLifetimeAllowance = Some(LifetimeAllowance(123.12, 123.12))
  )

  val responseJson: JsValue = Json.parse("""
      |{
      |    "pensionSchemeTaxReference": ["00123456RA"],
      |    "lumpSumBenefitTakenInExcessOfLifetimeAllowance": {
      |        "amount":123.12,
      |        "taxPaid":123.12
      |    },
      |    "benefitInExcessOfLifetimeAllowance": {
      |        "amount":123.12,
      |        "taxPaid":123.12
      |    }
      |}
      |""".stripMargin)

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        responseModel shouldBe responseJson.as[PensionSavingsTaxCharges]
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(responseModel) shouldBe responseJson
      }
    }
  }

}
