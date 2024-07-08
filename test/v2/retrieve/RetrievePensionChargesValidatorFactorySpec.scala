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

package v2.retrieve

import mocks.MockAppConfig
import support.UnitSpec
import v2.retrieve.def1.model.Def1_RetrievePensionChargesValidator

class RetrievePensionChargesValidatorFactorySpec extends UnitSpec with MockAppConfig {

  private val validNino      = "AA123456A"
  private val invalidNino    = "AA123456"
  private val validTaxYear   = "2021-22"
  private val invalidTaxYear = "202222"

  private val validatorFactory = new RetrievePensionChargesValidatorFactory(mockAppConfig)

  "validator" should {
    "return the Def1 validator" when {
      "given any valid request" in {
        val result = validatorFactory.validator(validNino, validTaxYear)
        result shouldBe a[Def1_RetrievePensionChargesValidator]
      }

      "given any invalid request" in {
        val result = validatorFactory.validator(invalidNino, invalidTaxYear)
        result shouldBe a[Def1_RetrievePensionChargesValidator]
      }
    }
  }

}
