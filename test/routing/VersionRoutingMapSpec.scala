/*
 * Copyright 2021 HM Revenue & Customs
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

package routing

import com.typesafe.config.ConfigFactory
import definition.Versions
import mocks.MockAppConfig
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import play.api.routing.Router
import support.UnitSpec

class VersionRoutingMapSpec extends UnitSpec with MockAppConfig with GuiceOneAppPerSuite {

  val defaultRouter: Router = mock[Router]
  val v1Routes: v1.Routes = app.injector.instanceOf[v1.Routes]
  val v1WithRelease6Routes: v1WithRelease6.Routes = app.injector.instanceOf[v1WithRelease6.Routes]

  "map" when {
    "routing to v1" when {
      def test(isRelease6Enabled: Boolean, routes: Any): Unit = {

        s"release 6 feature switch is set to - $isRelease6Enabled" should {
          s"route to ${routes.toString}" in {

            MockAppConfig.featureSwitch.returns(Some(Configuration(ConfigFactory.parseString(s"""
              |release-6.enabled = $isRelease6Enabled
              |""".stripMargin))))

            val versionRoutingMap: VersionRoutingMapImpl = VersionRoutingMapImpl(
              appConfig = mockAppConfig,
              defaultRouter = defaultRouter,
              v1Router = v1Routes,
              v1RouterWithRelease6 = v1WithRelease6Routes
            )

            versionRoutingMap.map(Versions.VERSION_1) shouldBe routes
          }
        }
      }

      Seq(
        (false, v1Routes),
        (true, v1WithRelease6Routes),
      ).foreach(args => (test _).tupled(args))
    }
  }
}
