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

package v2.createAmend

import api.utils.JsonErrorValidators
import mocks.MockIndividualsChargesConfig
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v2.createAmend.def1.model.Def1_CreateAmendPensionChargesValidator
import v2.createAmend.def2.model.Def2_CreateAmendPensionChargesValidator

class CreateAmendPensionChargesValidatorFactorySpec extends UnitSpec with JsonErrorValidators with MockIndividualsChargesConfig {

  private val validNino    = "AA123456A"
  private val validTaxYear = "2021-22"

  def requestBodyJson(): JsValue = Json.parse(
    s"""
       |{
       |
       |}
     """.stripMargin
  )

  private val validRequestBody = requestBodyJson()

  private val validatorFactory = new CreateAmendPensionChargesValidatorFactory

  private def setupMocks = MockedIndividualsChargesConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
    "removeLifetimePension.enabled" -> true
  )

  "validator" when {
    "given a valid taxYear before 2024-25" should {
      "return the Validator for schema definition 1" in {
        setupMocks
        val result = validatorFactory.validator(validNino, validTaxYear, validRequestBody)
        result shouldBe a[Def1_CreateAmendPensionChargesValidator]
      }
    }

    "given a valid taxYear 2024-25 or later" should {
      "return the Validator for schema definition 2" in {
        setupMocks
        val result = validatorFactory.validator(validNino, "2024-25", validRequestBody)
        result shouldBe a[Def2_CreateAmendPensionChargesValidator]
      }
    }

  }

}
