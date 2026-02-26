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

package routing

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.routing.Router
import shared.routing.Version3
import shared.utils.UnitSpec

class ChargesVersionRoutingMapSpec extends UnitSpec with GuiceOneAppPerSuite {

  val defaultRouter: Router = mock[Router]
  val v3Routes: v3.Routes   = app.injector.instanceOf[v3.Routes]

  "map" when {
    "routing to v3" should {
      val versionRoutingMap: ChargesVersionRoutingMap = ChargesVersionRoutingMap(
        defaultRouter = defaultRouter,
        v3Router = v3Routes
      )

      s"route to ${v3Routes.toString}" in {
        versionRoutingMap.map(Version3) shouldBe v3Routes
      }
    }
  }

}
