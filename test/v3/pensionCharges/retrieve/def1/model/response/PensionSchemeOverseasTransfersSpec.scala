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

package v3.pensionCharges.retrieve.def1.model.response

import play.api.libs.json.Json
import shared.utils.UnitSpec
import v3.pensionCharges.retrieve.def1.model.response.{OverseasSchemeProvider, PensionSchemeOverseasTransfers}

class PensionSchemeOverseasTransfersSpec extends UnitSpec {

  private val responseModel = PensionSchemeOverseasTransfers(
    Seq(
      OverseasSchemeProvider(
        "name",
        "address",
        "postcode",
        Some(Seq("Q123456")),
        None
      )),
    123.12,
    123.12)

  private val responseJson = Json.parse("""
      |{
      |     "overseasSchemeProvider": [
      |       {
      |         "providerName": "name",
      |         "providerAddress": "address",
      |         "providerCountryCode": "postcode",
      |         "qualifyingRecognisedOverseasPensionScheme": [
      |              "Q123456"
      |         ]
      |       }
      |     ],
      |     "transferCharge": 123.12,
      |     "transferChargeTaxPaid": 123.12
      |   }
      |""".stripMargin)

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        responseModel shouldBe responseJson.as[PensionSchemeOverseasTransfers]
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
