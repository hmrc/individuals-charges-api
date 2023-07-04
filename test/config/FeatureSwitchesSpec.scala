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

package config

import definition.{Version, Version1, Version2}
import play.api.Configuration
import support.UnitSpec

class FeatureSwitchesSpec extends UnitSpec {

  val anyVersion: Version = Version2

  "isCL102Enabled feature switch" should {
    "be true" when {

      "absent from the config" in {
        val configuration = Configuration.empty
        val featureSwitches = FeatureSwitches(configuration)

        featureSwitches.isCL102Enabled shouldBe true
      }

      "enabled" in {
        val configuration = Configuration("cl102.enabled" -> true)
        val featureSwitches = FeatureSwitches(configuration)

        featureSwitches.isCL102Enabled shouldBe true

      }
    }
  }

  "isVersionEnabled()" should {
    val configuration = Configuration(
      "version-1.enabled" -> true,
      "version-2.enabled" -> false
    )
    val featureSwitches = FeatureSwitches(configuration)

    "return false" when {
      "the version is blank" in {
        featureSwitches.isVersionEnabled(anyVersion) shouldBe false
      }

      "return true" when {
        "the version is enabled in the config" in {
          featureSwitches.isVersionEnabled(Version1) shouldBe true
        }
      }
    }
  }

}
