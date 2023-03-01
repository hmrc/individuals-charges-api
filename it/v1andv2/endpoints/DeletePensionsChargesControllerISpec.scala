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

package v1andv2.endpoints

import api.models.errors._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json._
import play.api.libs.ws._
import play.api.test.Helpers.AUTHORIZATION
import stubs.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import support.IntegrationBaseSpec

class DeletePensionsChargesControllerISpec extends IntegrationBaseSpec {

  val versions: Seq[String] = Seq("1.0", "2.0")

  private trait Test {

    val nino = "AA123456A"

    def taxYear: String
    def downstreamTaxYear: String

    def mtdUri: String = s"/pensions/$nino/$taxYear"

    def downstreamUri: String

    def setupStubs(): StubMapping

    def request(version: String): WSRequest = {
      setupStubs()
      buildRequest(mtdUri)
        .withHttpHeaders(
          (ACCEPT, s"application/vnd.hmrc.$version+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

    def errorBody(code: String): String =
      s"""{
         |  "failures": [
         |    {
         |      "code": "$code",
         |      "reason": "Downstream message."
         |    }
         |  ]
         |}
    """.stripMargin

  }

  private trait NonTysTest extends Test {
    def taxYear: String           = "2021-22"
    def downstreamTaxYear: String = "2021-22"
    def downstreamUri: String     = s"/income-tax/charges/pensions/$nino/$downstreamTaxYear"
  }

  private trait TysIfsTest extends Test {
    def taxYear: String           = "2023-24"
    def downstreamTaxYear: String = "23-24"
    def downstreamUri: String     = s"/income-tax/charges/pensions/$downstreamTaxYear/$nino"
  }

  "calling the delete endpoint" should {
    "return a 204 status" when {
      versions.foreach(testVersion =>
        s"any valid request is made in version $testVersion" in new NonTysTest with Test {

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DownstreamStub.onSuccess(DownstreamStub.DELETE, downstreamUri, NO_CONTENT, JsObject.empty)
          }

          val response: WSResponse = await(request(testVersion).delete())
          response.status shouldBe NO_CONTENT
          response.header("X-CorrelationId").nonEmpty shouldBe true
        })

      versions.foreach(testVersion =>
        s"any valid request with a Tax Year Specific (TYS) tax year is made in version $testVersion" in new TysIfsTest with Test {

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DownstreamStub.onSuccess(DownstreamStub.DELETE, downstreamUri, NO_CONTENT, JsObject.empty)
          }

          val response: WSResponse = await(request(testVersion).delete())
          response.status shouldBe NO_CONTENT
          response.header("X-CorrelationId").nonEmpty shouldBe true
        })
    }

    "return error according to spec" when {

      "validation error" when {
        def validationErrorTest(requestNino: String, requestTaxYear: String, expectedStatus: Int, expectedBody: MtdError, version: String): Unit = {
          s"validation fails with ${expectedBody.code} error in version $version" in new NonTysTest with Test {

            override val nino: String    = requestNino
            override val taxYear: String = requestTaxYear

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request(version).delete())
            response.status shouldBe expectedStatus
            response.json shouldBe expectedBody.asJson
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val input = Seq(
          ("Badnino", "2019-20", BAD_REQUEST, NinoFormatError),
          ("AA123456A", "203100", BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "2018-19", BAD_REQUEST, RuleTaxYearNotSupportedError),
          ("AA123456A", "2018-22", BAD_REQUEST, RuleTaxYearRangeInvalidError)
        )
        versions.foreach(version => input.foreach(args => validationErrorTest(args._1, args._2, args._3, args._4, version)))
      }

      "downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError, version: String): Unit = {
          s"downstream returns an $downstreamCode error and status $downstreamStatus in version $version" in new NonTysTest with Test {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DownstreamStub.onError(DownstreamStub.DELETE, downstreamUri, downstreamStatus, errorBody(downstreamCode))
            }

            val response: WSResponse = await(request(version).delete())
            response.status shouldBe expectedStatus
            response.json shouldBe expectedBody.asJson
          }
        }

        val errors = Seq(
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
          (NOT_FOUND, "NO_DATA_FOUND", NOT_FOUND, NotFoundError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
        )

        val extraTysErrors = Seq(
          (BAD_REQUEST, "INVALID_CORRELATION_ID", INTERNAL_SERVER_ERROR, InternalError),
          (NOT_FOUND, "NOT_FOUND", NOT_FOUND, NotFoundError),
          (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError)
        )

        versions.foreach(version => (errors ++ extraTysErrors).foreach(args => serviceErrorTest(args._1, args._2, args._3, args._4, version)))
      }
    }
  }

}
