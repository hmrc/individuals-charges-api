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

package config

import com.typesafe.config.ConfigFactory
import play.api.Configuration
import support.UnitSpec

class FeatureSwitchSpec extends UnitSpec {
  private def createFeatureSwitch(config: String) =
    FeatureSwitch(Some(Configuration(ConfigFactory.parseString(config))))

  "FeatureSwitch" when {
    "getting version enabled" when {
      "no config" must {
        val featureSwitch = FeatureSwitch(None)

        "return false" in {
          featureSwitch.isVersionEnabled("1.0") shouldBe false
        }
      }

      "no config value" must {
        val featureSwitch = createFeatureSwitch("")

        "return false" in {
          featureSwitch.isVersionEnabled("1.0") shouldBe false
        }
      }

      "config set" must {
        val featureSwitch = createFeatureSwitch(
          """
            |version-1.enabled = false
            |version-2.enabled = true
        """.stripMargin)

        "return false for disabled versions" in {
          featureSwitch.isVersionEnabled("1.0") shouldBe false
        }

        "return true for enabled versions" in {
          featureSwitch.isVersionEnabled("2.0") shouldBe true
        }

        "return false for non-version strings" in {
          featureSwitch.isVersionEnabled("x.x") shouldBe false
          featureSwitch.isVersionEnabled("2x") shouldBe false
          featureSwitch.isVersionEnabled("2.x") shouldBe false
        }
      }
    }

    "getting release-6.enabled" when {
      "no feature switch config available" must {
        "default to true" in {
          FeatureSwitch(None).isRelease6RoutingEnabled shouldBe true
        }
      }

      "config available but no release-6.enabled setting" must {
        "default to true" in {
          createFeatureSwitch("").isRelease6RoutingEnabled shouldBe true
        }
      }

      "config available and release-6.enabled set" in {
        createFeatureSwitch("release-6.enabled = true").isRelease6RoutingEnabled shouldBe true
        createFeatureSwitch("release-6.enabled = false").isRelease6RoutingEnabled shouldBe false
      }
    }
  }
}
