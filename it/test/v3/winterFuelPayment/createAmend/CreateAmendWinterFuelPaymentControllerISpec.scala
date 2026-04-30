/*
 * Copyright 2025 HM Revenue & Customs
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

package v3.winterFuelPayment.createAmend

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import common.errors.RuleOutsideAmendmentWindowError
import play.api.libs.json.*
import play.api.libs.ws.DefaultBodyReadables.readableAsString
import play.api.libs.ws.WSBodyWritables.writeableOf_JsValue
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.*
import shared.models.errors.*
import shared.models.utils.JsonErrorValidators
import shared.services.*
import shared.support.IntegrationBaseSpec
import v3.winterFuelPayment.createAmend.fixture.CreateAmendWinterFuelPaymentFixtures.*

class CreateAmendWinterFuelPaymentControllerISpec extends IntegrationBaseSpec with JsonErrorValidators {

  "Calling the 'Create Amend Winter Fuel Payment' endpoint" should {
    "return a 204 status code" when {
      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, Map("taxYear" -> "26-27"), NO_CONTENT, JsObject.empty)
        }

        val response: WSResponse = await(request().put(validRequestBodyJson))
        response.status shouldBe NO_CONTENT
        response.body shouldBe ""
      }
    }

    "return error according to spec" when {

      "validation error" when {
        def validationErrorTest(requestNino: String,
                                requestTaxYear: String,
                                requestBody: JsValue,
                                expectedStatus: Int,
                                expectedBody: MtdError,
                                errorWrapper: Option[ErrorWrapper]): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String    = requestNino
            override val taxYear: String = requestTaxYear

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val expectedBodyJson: JsValue = errorWrapper match {
              case Some(wrapper) => Json.toJson(wrapper)
              case None          => Json.toJson(expectedBody)
            }

            val response: WSResponse = await(request().put(requestBody))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBodyJson)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val input = Seq(
          ("AA1123A", "2026-27", validRequestBodyJson, BAD_REQUEST, NinoFormatError, None),
          ("AA123456A", "2026", validRequestBodyJson, BAD_REQUEST, TaxYearFormatError, None),
          ("AA123456A", "2026-28", validRequestBodyJson, BAD_REQUEST, RuleTaxYearRangeInvalidError, None),
          ("AA123456A", "2024-25", validRequestBodyJson, BAD_REQUEST, RuleTaxYearNotSupportedError, None),
          ("AA123456A", "2026-27", JsObject.empty, BAD_REQUEST, RuleIncorrectOrEmptyBodyError, None),
          ("AA123456A", "2026-27", validRequestBodyJson.update("/winterFuelPayment", JsNumber(123.123)), BAD_REQUEST, ValueFormatError.withPath("/winterFuelPayment"), None)
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
              DownstreamStub.onError(DownstreamStub.PUT, downstreamUri, Map("taxYear" -> "26-27"), downstreamStatus, errorBody(downstreamCode))

            }

            val response: WSResponse = await(request().put(validRequestBodyJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val errors = List(
          (BAD_REQUEST, "1000", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "1117", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "1215", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "1216", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "UNMATCHED_STUB_ERROR", BAD_REQUEST, RuleIncorrectGovTestScenarioError),
          (UNPROCESSABLE_ENTITY, "1115", BAD_REQUEST, RuleTaxYearNotEndedError),
          (UNPROCESSABLE_ENTITY, "1263", BAD_REQUEST, RuleWFPAmountAboveMaximumError),
          (UNPROCESSABLE_ENTITY, "1264", BAD_REQUEST, RuleWFPAmountBelowMinimumError),
          (UNPROCESSABLE_ENTITY, "4200", BAD_REQUEST, RuleOutsideAmendmentWindowError),
          (NOT_IMPLEMENTED, "5000", INTERNAL_SERVER_ERROR, InternalError)
        )

        errors.foreach(serviceErrorTest.tupled)
      }
    }
  }

  private trait Test {

    val nino = "AA123456A"

    def taxYear: String = "2026-27"

    def downstreamUri: String = s"/itsd/charges/winter-fuel-payment/$nino"

    def mtdUri: String = s"/winter-fuel-payment/$nino/$taxYear"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      AuthStub.resetAll()
      setupStubs()
      buildRequest(mtdUri)
        .withHttpHeaders(
          (ACCEPT, s"application/vnd.hmrc.3.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

    def errorBody(`type`: String): String =
      s"""
         |{
         |    "origin": "HIP",
         |    "response": {
         |        "failures": [
         |            {
         |                "type": "${`type`}",
         |                "reason": "downstream message"
         |            }
         |        ]
         |    }
         |}
       """.stripMargin

  }

}
