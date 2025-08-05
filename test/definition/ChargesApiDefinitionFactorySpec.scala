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

package definition

import cats.implicits.catsSyntaxValidatedId
import shared.config.Deprecation.NotDeprecated
import shared.config.{ConfidenceLevelConfig, MockSharedAppConfig}
import shared.definition.{APIDefinition, APIStatus, APIVersion, Definition}
import shared.routing.{Version2, Version3}
import shared.utils.UnitSpec
import uk.gov.hmrc.auth.core.ConfidenceLevel

class ChargesApiDefinitionFactorySpec extends UnitSpec with MockSharedAppConfig {

  private val confidenceLevel: ConfidenceLevel = ConfidenceLevel.L200

  "definition" when {
    "called" should {
      "return a valid Definition case class" in {
        MockedSharedAppConfig.apiGatewayContext returns "api.gateway.context"
        MockedSharedAppConfig.apiStatus(Version2) returns "2.0"
        MockedSharedAppConfig.endpointsEnabled(Version2) returns true
        MockedSharedAppConfig.apiStatus(Version3) returns "3.0"
        MockedSharedAppConfig.endpointsEnabled(Version3) returns true
        MockedSharedAppConfig.confidenceLevelConfig
          .returns(ConfidenceLevelConfig(confidenceLevel = confidenceLevel, definitionEnabled = true, authValidationEnabled = true))
          .anyNumberOfTimes()
        MockedSharedAppConfig.deprecationFor(Version2).returns(NotDeprecated.valid).anyNumberOfTimes()
        MockedSharedAppConfig.deprecationFor(Version3).returns(NotDeprecated.valid).anyNumberOfTimes()

        val apiDefinitionFactory = new ChargesApiDefinitionFactory(mockSharedAppConfig)
        apiDefinitionFactory.definition shouldBe
          Definition(
            api = APIDefinition(
              name = "Individuals Charges (MTD)",
              description = "This is a draft spec for the Individuals Charges API",
              context = "api.gateway.context",
              categories = Seq("INCOME_TAX_MTD"),
              versions = Seq(
                APIVersion(
                  version = Version2,
                  status = APIStatus.ALPHA,
                  endpointsEnabled = true
                ),
                APIVersion(
                  version = Version3,
                  status = APIStatus.ALPHA,
                  endpointsEnabled = true
                )
              ),
              requiresTrust = None
            )
          )
      }
    }
  }

}
