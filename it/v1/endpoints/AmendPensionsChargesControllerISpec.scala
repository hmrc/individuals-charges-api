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

import api.models.errors.{CountryCodeFormatError, MtdError, NinoFormatError, PensionSchemeTaxRefFormatError, ProviderAddressFormatError, ProviderNameFormatError, QOPSRefFormatError, RuleCountryCodeError, RuleIncorrectOrEmptyBodyError, RuleTaxYearNotSupportedError, StandardDownstreamError, TaxYearFormatError, ValueFormatError}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import v1.data.AmendPensionChargesData._
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import api.models.errors._
import v1.stubs.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}

class AmendPensionsChargesControllerISpec extends IntegrationBaseSpec {

  "Calling the amend endpoint" should {

    "return a 200 status code" when {

      "any valid request is made with the original data structure" in new NonTysTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, NO_CONTENT)
        }

        val response: WSResponse = await(mtdRequest.put(fullValidJson))
        response.status shouldBe OK
        response.json shouldBe hateoasResponse
        response.header("X-CorrelationId").nonEmpty shouldBe true
        response.header("Content-Type") shouldBe Some("application/json")
      }

        "any valid request is made with the updated data structure" in new NonTysTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, NO_CONTENT)
        }

        val response: WSResponse = await(mtdRequest.put(fullValidJsonUpdated))
        response.status shouldBe OK
        response.json shouldBe hateoasResponse
        response.header("X-CorrelationId").nonEmpty shouldBe true
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "a valid request is made for a Tax Year Specific tax year" in new TysIfsTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, NO_CONTENT)
        }

        val response: WSResponse = await(mtdRequest.put(fullValidJson))
        response.status shouldBe OK
        response.json shouldBe hateoasResponse
        response.header("X-CorrelationId").nonEmpty shouldBe true
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "any valid request is made with different booleans" in new NonTysTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, NO_CONTENT)
        }

        val response: WSResponse = await(mtdRequest.put(boolean1Json))
        response.status shouldBe OK
        response.json shouldBe hateoasResponse
        response.header("X-CorrelationId").nonEmpty shouldBe true
        response.header("Content-Type") shouldBe Some("application/json")

        val response2: WSResponse = await(mtdRequest.put(boolean2Json))
        response2.status shouldBe OK
        response2.json shouldBe hateoasResponse
        response2.header("X-CorrelationId").nonEmpty shouldBe true
        response2.header("Content-Type") shouldBe Some("application/json")

        val response3: WSResponse = await(mtdRequest.put(booleans3Json))
        response3.status shouldBe OK
        response3.json shouldBe hateoasResponse
        response3.header("X-CorrelationId").nonEmpty shouldBe true
        response3.header("Content-Type") shouldBe Some("application/json")

        val responseUpdated: WSResponse = await(mtdRequest.put(boolean1JsonUpdated))
        responseUpdated.status shouldBe OK
        responseUpdated.json shouldBe hateoasResponse
        responseUpdated.header("X-CorrelationId").nonEmpty shouldBe true
        responseUpdated.header("Content-Type") shouldBe Some("application/json")

        val response2Updated: WSResponse = await(mtdRequest.put(boolean2JsonUpdated))
        response2Updated.status shouldBe OK
        response2Updated.json shouldBe hateoasResponse
        response2Updated.header("X-CorrelationId").nonEmpty shouldBe true
        response2Updated.header("Content-Type") shouldBe Some("application/json")

        val response3Updated: WSResponse = await(mtdRequest.put(booleans3JsonUpdated))
        response3Updated.status shouldBe OK
        response3Updated.json shouldBe hateoasResponse
        response3Updated.header("X-CorrelationId").nonEmpty shouldBe true
        response3Updated.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {

      "validation error" when {
        def validationErrorTest(requestNino: String,
                                requestTaxYear: String,
                                requestBody: JsValue,
                                expectedStatus: Int,
                                expectedBody: MtdError): Unit = {

          s"validation fails with ${expectedBody.code} error ${if (expectedBody.equals(TaxYearFormatError)) java.util.UUID.randomUUID
            else ""}" in new NonTysTest {

            override val nino: String    = requestNino
            override val taxYear: String = requestTaxYear

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(requestNino)
            }

            val response: WSResponse = await(mtdRequest.put(requestBody))
            response.status shouldBe expectedStatus
            response.json shouldBe expectedBody.asJson
          }
        }

        val input = Seq(
          ("Badnino", "2019-20", fullValidJson, BAD_REQUEST, NinoFormatError),
          ("AA123456A", "beans", fullValidJson, BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "20!0-22", fullValidJson, BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "203100", fullValidJson, BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "2018-19", fullValidJson, BAD_REQUEST, RuleTaxYearNotSupportedError),
          ("AA123456A", "2021-22", invalidJson, BAD_REQUEST, RuleIncorrectOrEmptyBodyError),
          (
            "AA123456A",
            "2021-22",
            invalidNameJson,
            BAD_REQUEST,
            ProviderNameFormatError.copy(paths = Some(
              Seq(
                "/pensionSchemeOverseasTransfers/overseasSchemeProvider/0/providerName",
                "/overseasPensionContributions/overseasSchemeProvider/0/providerName")))
          ),
          (
            "AA123456A",
            "2021-22",
            invalidAddressJson,
            BAD_REQUEST,
            ProviderAddressFormatError.copy(paths = Some(
              Seq(
                "/pensionSchemeOverseasTransfers/overseasSchemeProvider/0/providerAddress",
                "/overseasPensionContributions/overseasSchemeProvider/0/providerAddress")))),
          (
            "AA123456A",
            "2021-22",
            fullReferencesJson("Q123456", "453"),
            BAD_REQUEST,
            PensionSchemeTaxRefFormatError.copy(paths = Some(Seq(
              "/pensionSchemeOverseasTransfers/overseasSchemeProvider/1/pensionSchemeTaxReference/0",
              "/overseasPensionContributions/overseasSchemeProvider/1/pensionSchemeTaxReference/0"
            )))),
          (
            "AA123456A",
            "2021-22",
            fullReferencesJson("234", "00123456RA"),
            BAD_REQUEST,
            QOPSRefFormatError.copy(paths = Some(Seq(
              "/pensionSchemeOverseasTransfers/overseasSchemeProvider/0/qualifyingRecognisedOverseasPensionScheme/0",
              "/overseasPensionContributions/overseasSchemeProvider/0/qualifyingRecognisedOverseasPensionScheme/0"
            )))),
          (
            "AA123456A",
            "2021-22",
            fullJsonWithInvalidCountryFormat("1YM"),
            BAD_REQUEST,
            RuleCountryCodeError.copy(paths = Some(Seq(
              "/pensionSchemeOverseasTransfers/overseasSchemeProvider/2/providerCountryCode",
              "/overseasPensionContributions/overseasSchemeProvider/2/providerCountryCode"
            )))),
          (
            "AA123456A",
            "2021-22",
            fullJsonWithInvalidCountryFormat("INVALID"),
            BAD_REQUEST,
            CountryCodeFormatError.copy(paths = Some(Seq(
              "/pensionSchemeOverseasTransfers/overseasSchemeProvider/2/providerCountryCode",
              "/overseasPensionContributions/overseasSchemeProvider/2/providerCountryCode"
            )))),
          (
            "AA123456A",
            "2021-22",
            fullJson(999999999999.99),
            BAD_REQUEST,
            ValueFormatError.copy(paths = Some(Seq(
              "/pensionSavingsTaxCharges/lumpSumBenefitTakenInExcessOfLifetimeAllowance/amount",
              "/pensionSavingsTaxCharges/lumpSumBenefitTakenInExcessOfLifetimeAllowance/taxPaid",
              "/pensionSchemeOverseasTransfers/transferChargeTaxPaid",
              "/pensionSchemeOverseasTransfers/transferCharge",
              "/pensionSchemeUnauthorisedPayments/surcharge/amount",
              "/pensionSchemeUnauthorisedPayments/surcharge/foreignTaxPaid",
              "/pensionSchemeUnauthorisedPayments/noSurcharge/amount",
              "/pensionSchemeUnauthorisedPayments/noSurcharge/foreignTaxPaid",
              "/pensionContributions/annualAllowanceTaxPaid",
              "/pensionContributions/inExcessOfTheAnnualAllowance",
              "/overseasPensionContributions/shortServiceRefund",
              "/overseasPensionContributions/shortServiceRefundTaxPaid"
            ))))
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
              DownstreamStub.onError(DownstreamStub.PUT, downstreamUri, downstreamStatus, errorBody(downstreamCode))
            }

            val response: WSResponse = await(mtdRequest.put(fullValidJson))
            response.status shouldBe expectedStatus
            response.json shouldBe expectedBody.asJson
          }
        }

        def errorBody(code: String): String =
          s"""
             |{
             |   "code": "$code",
             |   "reason": "message"
             |}
            """.stripMargin

        val errors = Seq(
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (BAD_REQUEST, "INVALID_PAYLOAD", BAD_REQUEST, RuleIncorrectOrEmptyBodyError),
          (UNPROCESSABLE_ENTITY, "REDUCTION_TYPE_NOT_SPECIFIED", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (UNPROCESSABLE_ENTITY, "REDUCTION_NOT_SPECIFIED", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, StandardDownstreamError)
        )

        val extraTysErrors = Seq(
          (UNPROCESSABLE_ENTITY, "MISSING_ANNUAL_ALLOWANCE_REDUCTION", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (UNPROCESSABLE_ENTITY, "MISSING_TYPE_OF_REDUCTION", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError)
        )

        (errors ++ extraTysErrors).foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

  private trait Test {

    def taxYear: String
    def downstreamUri: String

    val nino: String = "AA123456A"

    val hateoasResponse: JsValue = Json.parse(
      s"""
         |{
         |   "links":[
         |      {
         |         "href":"/individuals/charges/pensions/$nino/$taxYear",
         |         "method":"GET",
         |         "rel":"self"
         |      },
         |      {
         |         "href":"/individuals/charges/pensions/$nino/$taxYear",
         |         "method":"PUT",
         |         "rel":"create-and-amend-charges-pensions"
         |      },
         |      {
         |         "href":"/individuals/charges/pensions/$nino/$taxYear",
         |         "method":"DELETE",
         |         "rel":"delete-charges-pensions"
         |      }
         |   ]
         |}
          """.stripMargin
    )

    def setupStubs(): StubMapping

    def mtdRequest: WSRequest = {
      setupStubs()
      buildRequest(s"/pensions/$nino/$taxYear")
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

  }

  private trait NonTysTest extends Test {

    def taxYear: String       = "2021-22"
    def downstreamUri: String = s"/income-tax/charges/pensions/$nino/2021-22"
  }

  private trait TysIfsTest extends Test {

    def taxYear: String       = "2023-24"
    def downstreamUri: String = s"/income-tax/charges/pensions/23-24/$nino"
  }

}
