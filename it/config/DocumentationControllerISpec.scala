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

import io.swagger.v3.parser.OpenAPIV3Parser
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSResponse
import routing.{Version2, Version3}
import support.IntegrationBaseSpec

import scala.util.Try

class DocumentationControllerISpec extends IntegrationBaseSpec {

  private val config          = app.injector.instanceOf[AppConfig]
  private val confidenceLevel = config.confidenceLevelConfig.confidenceLevel

  val apiDefinitionJson: JsValue = Json.parse(s"""
      |{
      |  "scopes":[
      |    {
      |      "key":"read:self-assessment",
      |      "name":"View your Self Assessment information",
      |      "description":"Allow read access to self assessment data",
      |      "confidenceLevel": $confidenceLevel
      |    },
      |    {
      |      "key":"write:self-assessment",
      |      "name":"Change your Self Assessment information",
      |      "description":"Allow write access to self assessment data",
      |      "confidenceLevel": $confidenceLevel
      |    }
      |  ],
      |  "api":{
      |    "name":"Individuals Charges (MTD)",
      |    "description":"This is a draft spec for the Individuals Charges API",
      |    "context":"individuals/charges",
      |    "categories":["INCOME_TAX_MTD"],
      |    "versions":[
      |      {
      |        "version":"2.0",
      |        "status":"ALPHA",
      |        "endpointsEnabled":true
      |       },
      |       {
      |        "version":"3.0",
      |        "status":"BETA",
      |        "endpointsEnabled":false
      |       }
      |    ]
      |  }
      |}
    """.stripMargin)

  "GET /api/definition" should {
    "return a 200 with the correct response body" in {
      val response: WSResponse = await(buildRequest("/api/definition").get())
      response.status shouldBe Status.OK
      Json.parse(response.body) shouldBe apiDefinitionJson
    }
  }

  "an OAS documentation request" must {

    List(Version2, Version3).foreach { version =>
      s"return the documentation that passes OAS V3 parser for version $version" in {
        val response: WSResponse = await(buildRequest(s"/api/conf/${version.name}/application.yaml").get())
        response.status shouldBe Status.OK

        val contents     = response.body
        val parserResult = Try(new OpenAPIV3Parser().readContents(contents))
        parserResult.isSuccess shouldBe true

        val openAPI = Option(parserResult.get.getOpenAPI)
        openAPI.isEmpty shouldBe false
        openAPI.get.getOpenapi shouldBe "3.0.3"
        openAPI.get.getInfo.getTitle shouldBe "Individuals Charges (MTD)"
        openAPI.get.getInfo.getVersion shouldBe version.name
      }

      s"return the documentation with the correct accept header for version $version" in {
        val response: WSResponse = await(buildRequest(s"/api/conf/${version.name}/common/headers.yaml").get())
        response.status shouldBe Status.OK
        val contents = response.body

        val headerRegex = """(?s).*?application/vnd\.hmrc\.(\d+\.\d+)\+json.*?""".r
        val header      = headerRegex.findFirstMatchIn(contents)
        header.isDefined shouldBe true

        val versionFromHeader = header.get.group(1)
        versionFromHeader shouldBe version.name

      }
    }
  }

}
