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

package v3.highIncomeChildBenefitCharge.delete

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import common.errors.RuleOutsideAmendmentWindow
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import shared.models.domain.TaxYear
import shared.models.errors._
import shared.services._
import shared.support.IntegrationBaseSpec
class DeleteHighIncomeChildBenefitChargeControllerISpec extends IntegrationBaseSpec {

  "Calling the 'delete high income child benefit charge' endpoint" should {
    "return a 204 status code" when {
      "any valid request is made" in new Test  {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.DELETE, downstreamUri, NO_CONTENT, JsObject.empty)
          }

        val response: WSResponse = await(request().delete())
        response.status shouldBe NO_CONTENT
        response.body shouldBe ""

      }
    }

    "return error according to spec" when {

      "validation error" when {
        def validationErrorTest(requestNino: String, requestTaxYear: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String    = requestNino
            override val taxYear: String = requestTaxYear

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request().delete())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val input = Seq(
          ("AA1123A", "2025-26", BAD_REQUEST, NinoFormatError),
          ("AA123456A", "20477",  BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "2025-27",  BAD_REQUEST, RuleTaxYearRangeInvalidError),
          ("AA123456A", "2024-25",  BAD_REQUEST, RuleTaxYearNotSupportedError)
        )
        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns an $downstreamCode error and status $downstreamStatus" in new Test {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DownstreamStub.onError(DownstreamStub.DELETE, downstreamUri, downstreamStatus, errorBody(downstreamCode))
            }

            val response: WSResponse = await(request().delete())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val errors = List(
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
          (NOT_FOUND, "NOT_FOUND", NOT_FOUND, NotFoundError),
          (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError),
          (UNPROCESSABLE_ENTITY, "OUTSIDE_AMENDMENT_WINDOW", BAD_REQUEST, RuleOutsideAmendmentWindow),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
        )

        errors.foreach(args => (serviceErrorTest _).tupled(args))
      }

    }
  }

  private trait Test {

    val nino = "AA123456A"

    def taxYear: String           = "2025-26"

    def downstreamUri: String = s"/itsa/income-tax/v1/${TaxYear.fromMtd(taxYear).asTysDownstream}/high-income-child-benefit/charges/$nino"

    def mtdUri: String = s"/high-income-child-benefit/$nino/$taxYear"

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
