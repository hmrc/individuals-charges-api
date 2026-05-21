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

package v3.winterFuelPayment.delete

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import common.errors.RuleOutsideAmendmentWindowError
import play.api.libs.json.*
import play.api.libs.ws.*
import play.api.test.Helpers.*
import api.models.errors.*
import api.services.*
import api.support.IntegrationBaseSpec

class DeleteWinterFuelPaymentISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino = "AA123456A"

    val taxYear: String = "2026-27"

    def downstreamUri: String = s"/itsd/charges/winter-fuel-payment/$nino"

    def mtdUri: String = s"/winter-fuel-payment/$nino/$taxYear"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      AuthStub.resetAll()
      setupStubs()
      buildRequest(mtdUri)
        .withHttpHeaders(
          (ACCEPT, s"application/vnd.hmrc.3.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
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

  }

  "calling the delete endpoint" should {
    "return a 204 status" when {
      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.DELETE, downstreamUri, Map("taxYear" -> "26-27"), NO_CONTENT, JsObject.empty)
        }

        val response: WSResponse = await(request().delete())

        response.status shouldBe NO_CONTENT
        response.body[String] shouldBe ""
        response.header("Content-Type") shouldBe None
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }
    }

    "return error according to spec" when {

      "validation error" when {
        def validationErrorTest(requestNino: String, requestTaxYear: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code}" in new Test {

            override val nino: String    = requestNino
            override val taxYear: String = requestTaxYear

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request().delete())

            response.status shouldBe expectedStatus
            response.json shouldBe expectedBody.asJson
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val input = Seq(
          ("Badnino", "2026-27", BAD_REQUEST, NinoFormatError),
          ("AA123456A", "203100", BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "2025-26", BAD_REQUEST, RuleTaxYearNotSupportedError),
          ("AA123456A", "2026-28", BAD_REQUEST, RuleTaxYearRangeInvalidError)
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
              DownstreamStub.onError(DownstreamStub.DELETE, downstreamUri, Map("taxYear" -> "26-27"), downstreamStatus, errorBody(downstreamCode))
            }

            val response: WSResponse = await(request().delete())

            response.status shouldBe expectedStatus
            response.json shouldBe expectedBody.asJson
            response.header("X-CorrelationId").nonEmpty shouldBe true
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val errors = List(
          (BAD_REQUEST, "1117", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "1215", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "1216", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "UNMATCHED_STUB_ERROR", BAD_REQUEST, RuleIncorrectGovTestScenarioError),
          (NOT_FOUND, "5010", NOT_FOUND, NotFoundError),
          (UNPROCESSABLE_ENTITY, "4200", BAD_REQUEST, RuleOutsideAmendmentWindowError),
          (NOT_IMPLEMENTED, "5000", INTERNAL_SERVER_ERROR, InternalError)
        )

        errors.foreach(serviceErrorTest.tupled)
      }
    }
  }

}
