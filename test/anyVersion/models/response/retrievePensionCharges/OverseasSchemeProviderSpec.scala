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

package anyVersion.models.response.retrievePensionCharges

import play.api.libs.json.Json
import support.UnitSpec

class OverseasSchemeProviderSpec extends UnitSpec {

  val responseModel = OverseasSchemeProvider(
    "Overseas Pensions Plc",
    "111 Main Street, George Town, Grand Cayman",
    "CYM",
    Some(Seq("Q123456")),
    None
  )

  val responseJson = Json.parse("""
      |{
      |        "providerName": "Overseas Pensions Plc",
      |        "providerAddress": "111 Main Street, George Town, Grand Cayman",
      |        "providerCountryCode": "CYM",
      |        "qualifyingRecognisedOverseasPensionScheme": [
      |          "Q123456"
      |        ]
      |      }
      |""".stripMargin)

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        responseModel shouldBe responseJson.as[OverseasSchemeProvider]
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
