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

package routing

import play.api.http.HeaderNames.ACCEPT
import play.api.libs.json.{JsError, JsResult, JsString, JsSuccess, JsValue, Json}
import play.api.test.FakeRequest
import routing.Version.VersionReads
import support.UnitSpec

class VersionsSpec extends UnitSpec {

  "Versions" when {
    "retrieved from a request header" must {
      "return an error if the version is unsupported" in {
        Versions.getFromRequest(FakeRequest().withHeaders((ACCEPT, "application/vnd.hmrc.4.0+json"))) shouldBe Left(VersionNotFound)
      }

      "return an error if the Accept header value is invalid" in {
        Versions.getFromRequest(FakeRequest().withHeaders((ACCEPT, "application/XYZ.2.0+json"))) shouldBe Left(InvalidHeader)
      }

      "return the specified version 2" in {
        Versions.getFromRequest(FakeRequest().withHeaders((ACCEPT, "application/vnd.hmrc.2.0+json"))) shouldBe Right(Version2)
      }

      "return the specified version 3" in {
        Versions.getFromRequest(FakeRequest().withHeaders((ACCEPT, "application/vnd.hmrc.3.0+json"))) shouldBe Right(Version3)
      }
    }

    "retrieved from a name" must {
      "return an error if the name is unsupported" in {
        Versions.getFrom("1.0") shouldBe Left(VersionNotFound)
      }

      "return the specified version" in {
        Versions.getFrom("2.0") shouldBe Right(Version2)
      }
    }
  }

  "VersionReads" should {
    "successfully read Version2" in {
      val versionJson: JsValue      = JsString(Version2.name)
      val result: JsResult[Version] = VersionReads.reads(versionJson)

      result shouldEqual JsSuccess(Version2)
    }

    "return error for unrecognised version" in {
      val versionJson: JsValue      = JsString("UnknownVersion")
      val result: JsResult[Version] = VersionReads.reads(versionJson)

      result shouldBe a[JsError]
    }
  }

  "serialized to Json" must {
    "return the expected Json output" in {
      val version: Version = Version2
      val expected         = Json.parse(""" "2.0" """)
      val result           = Json.toJson(version)
      result shouldBe expected
    }
  }

  "toString" should {
    "return the version name" in {
      val result = Version2.toString
      result shouldBe Version2.name
    }
  }

}
