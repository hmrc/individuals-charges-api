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

package v3.highIncomeChildBenefitCharge.createAmend

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import common.errors.RuleOutsideAmendmentWindowError
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE, UNPROCESSABLE_ENTITY}
import play.api.libs.json._
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers._
import shared.models.domain.TaxYear
import shared.models.errors._
import shared.models.utils.JsonErrorValidators
import shared.services._
import shared.support.IntegrationBaseSpec
import v3.highIncomeChildBenefitCharge.createAmend.fixture.CreateAmendHighIncomeChildBenefitChargeFixtures._

class CreateAmendHighIncomeChildBenefitChargeControllerISpec extends IntegrationBaseSpec with JsonErrorValidators {

  "Calling the 'Create or Amend High Income Child Benefit Charge' endpoint" should {
    "return a 204 status code" when {
      "any valid request is made" in new Test  {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, NO_CONTENT, JsObject.empty)
        }

        val response: WSResponse = await(request().put(validFullRequestBodyJson))
        response.status shouldBe NO_CONTENT
        response.body shouldBe ""
      }
    }

    "return error according to spec" when {

      val incorrectlyFormattedRequestBodyJson: JsValue = Json.parse(
        s"""
          |{
          |  "amountOfChildBenefitReceived": -1111.22,
          |  "numberOfChildren": -2,
          |  "dateCeased": "2024"
          |}
        """.stripMargin
      )

      "validation error" when {
        def validationErrorTest(requestNino: String,
                                requestTaxYear: String,
                                requestBody: JsValue,
                                expectedStatus: Int,
                                expectedBody: MtdError,
                                errorWrapper: Option[ErrorWrapper]): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String         = requestNino
            override val taxYear: String      = requestTaxYear

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
          ("AA1123A", "2025-26", validFullRequestBodyJson, BAD_REQUEST, NinoFormatError, None),
          ("AA123456A", "2025", validFullRequestBodyJson, BAD_REQUEST, TaxYearFormatError, None),
          ("AA123456A", "2025-27", validFullRequestBodyJson, BAD_REQUEST, RuleTaxYearRangeInvalidError, None),
          ("AA123456A", "2024-25", validFullRequestBodyJson, BAD_REQUEST, RuleTaxYearNotSupportedError, None),
          ("AA123456A", "2025-26", JsObject.empty, BAD_REQUEST, RuleIncorrectOrEmptyBodyError, None),
          (
            "AA123456A",
            "2025-26",
            validFullRequestBodyJson.update("/dateCeased", JsString("2026-04-06")),
            BAD_REQUEST,
            RuleDateCeasedError,
            None
          ),
          (
            "AA123456A",
            "2025-26",
            incorrectlyFormattedRequestBodyJson,
            BAD_REQUEST,
            BadRequestError,
            Some(
              ErrorWrapper(
                "123",
                BadRequestError,
                Some(
                  List(
                    DateCeasedFormatError,
                    ValueFormatError.forPathAndRange("/amountOfChildBenefitReceived", "0", "99999999999.99"),
                    ValueFormatError.forIntegerPathAndRange("/numberOfChildren", "1", "99")
                  )
                )
              )
            )
          )
        )

        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns a code $downstreamCode error and status $downstreamStatus" in new Test {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DownstreamStub.onError(DownstreamStub.PUT, downstreamUri, downstreamStatus, errorBody(downstreamCode))

            }

            val response: WSResponse = await(request().put(validFullRequestBodyJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val errors = List(
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError),
          (UNPROCESSABLE_ENTITY, "OUTSIDE_AMENDMENT_WINDOW", BAD_REQUEST, RuleOutsideAmendmentWindowError),
          (UNPROCESSABLE_ENTITY, "INVALID_RANGE_OF_NO_OF_CHILDREN", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "INVALID_DATE_CEASED", INTERNAL_SERVER_ERROR, InternalError),
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
