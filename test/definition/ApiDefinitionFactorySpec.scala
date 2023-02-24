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

package definition

import config.ConfidenceLevelConfig
import definition.APIStatus.{ALPHA, BETA}
import mocks.{MockAppConfig, MockHttpClient}
import support.UnitSpec
import uk.gov.hmrc.auth.core.ConfidenceLevel

class ApiDefinitionFactorySpec extends UnitSpec {

  class Test extends MockHttpClient with MockAppConfig {
    val apiDefinitionFactory = new ApiDefinitionFactory(mockAppConfig)
    MockAppConfig.apiGatewayContext returns "api.gateway.context"

    def confidenceLevel: ConfidenceLevel =
      if (mockAppConfig.confidenceLevelConfig.definitionEnabled) ConfidenceLevel.L200 else ConfidenceLevel.L50

  }

  "buildAPIStatus" when {
    "the 'apiStatus' parameter is present and valid" should {
      Seq(
        (Version1, ALPHA),
        (Version2, BETA)
      ).foreach { case (version, status) =>
        s"return the correct $status for $version " in new Test {
          MockAppConfig.apiStatus(version) returns status.toString
          apiDefinitionFactory.buildAPIStatus(version) shouldBe status
        }
      }
    }

    "the 'apiStatus' parameter is present and invalid" should {
      Seq(Version1, Version2).foreach { version =>
        s"default to alpha for $version " in new Test {
          MockAppConfig.apiStatus(version) returns "ALPHO"
          apiDefinitionFactory.buildAPIStatus(version) shouldBe ALPHA
        }
      }
    }
  }

  "confidenceLevel" when {
    Seq(
      (true, ConfidenceLevel.L200),
      (false, ConfidenceLevel.L50)
    ).foreach { case (definitionEnabled, cl) =>
      s"confidence-level-check.definition.enabled is $definitionEnabled in config" should {
        s"return $cl" in new Test {
          MockAppConfig.confidenceLevelCheckEnabled returns ConfidenceLevelConfig(definitionEnabled = definitionEnabled, authValidationEnabled = true)
          apiDefinitionFactory.confidenceLevel shouldBe cl
        }
      }
    }
  }

}
