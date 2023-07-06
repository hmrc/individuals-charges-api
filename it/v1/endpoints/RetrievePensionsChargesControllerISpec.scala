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

package v1.endpoints

import api.models.errors._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import stubs.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import support.IntegrationBaseSpec
import v1.data.RetrievePensionChargesData.{fullJsonCL102FieldsInBoth, fullJsonWithHateoas}

class RetrievePensionsChargesControllerISpec extends IntegrationBaseSpec {

  private trait NonTysTest extends Test {
    def mtdTaxYear: String        = "2021-22"
    def downstreamTaxYear: String = "2021-22"
    def downstreamUri: String     = s"/income-tax/charges/pensions/$nino/$downstreamTaxYear"
  }

  private trait TysIfsTest extends Test {
    def mtdTaxYear: String        = "2023-24"
    def downstreamTaxYear: String = "23-24"
    def downstreamUri: String     = s"/income-tax/charges/pensions/$downstreamTaxYear/$nino"
  }

  private trait Test {

    val nino = "AA123456A"

    def mtdTaxYear: String
    def downstreamTaxYear: String
    def downstreamUri: String
    def setupStubs(): StubMapping

    def uri: String = s"/pensions/$nino/$mtdTaxYear"

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

    def errorBody(code: String): String =
      s"""
         |      {
         |        "code": "$code",
         |        "reason": "downstream message"
         |      }
    """.stripMargin

  }

  "Calling the retrieve endpoint" should {

    "return a 200 status code" when {

      "any valid Non-TYS request is made" in new NonTysTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, OK, fullJsonCL102FieldsInBoth)
        }

        val response: WSResponse = await(request().get())

        response.status shouldBe OK
        response.json shouldBe fullJsonWithHateoas(mtdTaxYear)
        response.header("X-CorrelationId").nonEmpty shouldBe true
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "any valid request is made with a Tax Year Specific year" in new TysIfsTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, OK, fullJsonCL102FieldsInBoth)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe OK
        response.json shouldBe fullJsonWithHateoas(mtdTaxYear)
        response.header("X-CorrelationId").nonEmpty shouldBe true
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {

      "validation error" when {
        def validationErrorTest(requestNino: String, requestTaxYear: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new NonTysTest {

            override val nino: String       = requestNino
            override def mtdTaxYear: String = requestTaxYear

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(requestNino)
            }

            val response: WSResponse = await(request().get())
            response.status shouldBe expectedStatus
            response.json shouldBe expectedBody.asJson
          }
        }

        val input = Seq(
          ("Badnino", "2019-20", BAD_REQUEST, NinoFormatError),
          ("AA123456A", "203100", BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "2018-19", BAD_REQUEST, RuleTaxYearNotSupportedError),
          ("AA123456A", "2018-22", BAD_REQUEST, RuleTaxYearRangeInvalidError)
        )
        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns an $downstreamCode error and status $downstreamStatus" in new NonTysTest {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DownstreamStub.onError(DownstreamStub.GET, downstreamUri, downstreamStatus, errorBody(downstreamCode))
            }

            val response: WSResponse = await(request().get())
            response.status shouldBe expectedStatus
            response.json shouldBe expectedBody.asJson
          }
        }

        val input = Seq(
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
          (NOT_FOUND, "NO_DATA_FOUND", NOT_FOUND, NotFoundError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
        )

        val extraTysErrors = Seq(
          (NOT_FOUND, "NOT_FOUND", NOT_FOUND, NotFoundError),
          (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError)
        )
        (input ++ extraTysErrors).foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

}
