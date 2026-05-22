/*
 * Copyright 2026 HM Revenue & Customs
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

package v3.winterFuelPayment.retrieve

import api.models.domain.MtdSourceEnum
import api.models.errors.*
import api.services.*
import api.support.IntegrationBaseSpec
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import common.errors.SourceFormatError
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.*
import v3.winterFuelPayment.retrieve.RetrieveWinterFuelPaymentFixtures.{responseDownstreamJson, responseMtdJson}

class RetrieveWinterFuelPaymentControllerISpec extends IntegrationBaseSpec {

  "Calling the 'Retrieve Winter Fuel Payment' endpoint" should {
    "return a 200 status code" when {
      "a valid request is made without source parameter" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, downstreamQueryParams, OK, responseDownstreamJson)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe OK
        response.json shouldBe responseMtdJson
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return a 200 status code with source query parameter" when {
      "a valid request is made with source parameter" in new Test {
        override val maybeSource: Option[String] = Some("hmrc-held")

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, downstreamQueryParams, OK, responseDownstreamJson)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe OK
        response.json shouldBe responseMtdJson
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {

      "validation error" when {
        def validationErrorTest(requestNino: String,
                                requestTaxYear: String,
                                requestMaybeSource: Option[String],
                                expectedStatus: Int,
                                expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String                = requestNino
            override val taxYear: String             = requestTaxYear
            override val maybeSource: Option[String] = requestMaybeSource

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request().get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val input = Seq(
          ("AA1123A", "2026-27", None, BAD_REQUEST, NinoFormatError),
          ("AA123456A", "20267", None, BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "2026-27", Some("invalid-source"), BAD_REQUEST, SourceFormatError),
          ("AA123456A", "2026-28", None, BAD_REQUEST, RuleTaxYearRangeInvalidError),
          ("AA123456A", "2025-26", None, BAD_REQUEST, RuleTaxYearNotSupportedError)
        )

        input.foreach(validationErrorTest.tupled)
      }

      "downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns a code $downstreamCode error and status $downstreamStatus" in new Test {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DownstreamStub.onError(DownstreamStub.GET, downstreamUri, downstreamQueryParams, downstreamStatus, errorBody(downstreamCode))
            }

            val response: WSResponse = await(request().get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        def errorBody(code: String): String =
          s"""
             |[
             |  {
             |   "errorCode": "$code",
             |   "errorDescription": "message"
             |  }
             |]
          """.stripMargin

        val errors = List(
          (BAD_REQUEST, "1117", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "1215", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "1216", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "1239", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "UNMATCHED_STUB_ERROR", BAD_REQUEST, RuleIncorrectGovTestScenarioError),
          (NOT_FOUND, "5010", NOT_FOUND, NotFoundError),
          (NOT_IMPLEMENTED, "5000", INTERNAL_SERVER_ERROR, InternalError)
        )

        errors.foreach(serviceErrorTest.tupled)
      }
    }
  }

  private trait Test {

    val nino: String = "AA123456A"

    def taxYear: String = "2026-27"

    val maybeSource: Option[String] = None

    def downstreamUri: String = s"/itsd/charges/winter-fuel-payment/$nino"

    def downstreamQueryParams: Map[String, String] = {
      val source = maybeSource.flatMap(MtdSourceEnum.parser.lift).getOrElse(MtdSourceEnum.latest)
      Map("taxYear" -> "26-27", "view" -> source.toDownstreamViewString)
    }

    private def uri: String = s"/winter-fuel-payment/$nino/$taxYear"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()

      val baseRequest = buildRequest(uri).withHttpHeaders(
        (ACCEPT, "application/vnd.hmrc.3.0+json"),
        (AUTHORIZATION, "Bearer 123")
      )

      maybeSource.fold(baseRequest)(source => baseRequest.addQueryStringParameters("source" -> source))
    }

  }

}
