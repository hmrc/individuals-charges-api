/*
 * Copyright 2024 HM Revenue & Customs
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

package v2.retrieve

import play.api.Configuration
import shared.config.MockSharedAppConfig
import shared.utils.UnitSpec
import v2.retrieve.def1.model.Def1_RetrievePensionChargesValidator
import v2.retrieve.def2.model.Def2_RetrievePensionChargesValidator

class RetrievePensionChargesValidatorFactorySpec extends UnitSpec with MockSharedAppConfig {

  private val validNino        = "AA123456A"
  private val validDef1TaxYear = "2021-22"
  private val validDef2TaxYear = "2024-25"

  private val validatorFactory = new RetrievePensionChargesValidatorFactory(mockSharedAppConfig)

  private def setupMocks = MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
    "removeLifetimePension.enabled" -> true
  )

  "validator" should {
    "return the Def1 validator" when {
      "given any valid request" in {
        setupMocks
        val result = validatorFactory.validator(validNino, validDef1TaxYear)
        result shouldBe a[Def1_RetrievePensionChargesValidator]
      }

      "given an valid request and featureSwitch is disabled" in {
        MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration("removeLifetimePension.enabled" -> false)
        val result = validatorFactory.validator(validNino, validDef2TaxYear)
        result shouldBe a[Def1_RetrievePensionChargesValidator]
      }
    }

    "return the Def2 validator" when {
      "given any valid request" in {
        setupMocks
        val result = validatorFactory.validator(validNino, validDef2TaxYear)
        result shouldBe a[Def2_RetrievePensionChargesValidator]
      }
    }

  }

}
