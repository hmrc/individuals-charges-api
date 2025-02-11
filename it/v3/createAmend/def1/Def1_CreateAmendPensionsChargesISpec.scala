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

package v3.createAmend.def1

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import common.errors.{PensionSchemeTaxRefFormatError, ProviderAddressFormatError, ProviderNameFormatError, QOPSRefFormatError, RuleOutsideAmendmentWindow}
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.JsValue
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import shared.models.errors._
import shared.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import shared.support.IntegrationBaseSpec
import v3.createAmend.def1.fixture.Def1_CreateAmendPensionChargesFixture._

class Def1_CreateAmendPensionsChargesISpec extends IntegrationBaseSpec {

  "Calling the create & amend endpoint" should {

    "return a 204 status code" when {

      "any valid request is made with the original data structure" in new NonTysTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, NO_CONTENT)
        }

        val response: WSResponse = await(mtdRequest.put(fullValidJson))
        response.status shouldBe NO_CONTENT
        response.header("X-CorrelationId").nonEmpty shouldBe true
        response.header("Content-Type") shouldBe None
      }

      "a valid request is made for a Tax Year Specific tax year" in new IfsTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, NO_CONTENT)
        }

        val response: WSResponse = await(mtdRequest.put(fullValidJson))
        response.status shouldBe NO_CONTENT
        response.header("X-CorrelationId").nonEmpty shouldBe true
        response.header("Content-Type") shouldBe None
      }

      "any valid request is made with different booleans" in new NonTysTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, NO_CONTENT)
        }

        val response: WSResponse = await(mtdRequest.put(boolean1Json))
        response.status shouldBe NO_CONTENT
        response.header("X-CorrelationId").nonEmpty shouldBe true
        response.header("Content-Type") shouldBe None

        val response2: WSResponse = await(mtdRequest.put(boolean2Json))
        response2.status shouldBe NO_CONTENT
        response2.header("X-CorrelationId").nonEmpty shouldBe true
        response2.header("Content-Type") shouldBe None

        val response3: WSResponse = await(mtdRequest.put(booleans3Json))
        response3.status shouldBe NO_CONTENT
        response3.header("X-CorrelationId").nonEmpty shouldBe true
        response3.header("Content-Type") shouldBe None

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
          (
            "AA123456A",
            "2021-22",
            invalidJson,
            BAD_REQUEST,
            RuleIncorrectOrEmptyBodyError.withPath("/pensionSavingsTaxCharges/pensionSchemeTaxReference")),
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
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_PAYLOAD", BAD_REQUEST, RuleIncorrectOrEmptyBodyError),
          (UNPROCESSABLE_ENTITY, "REDUCTION_TYPE_NOT_SPECIFIED", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "REDUCTION_NOT_SPECIFIED", INTERNAL_SERVER_ERROR, InternalError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "OUTSIDE_AMENDMENT_WINDOW", BAD_REQUEST, RuleOutsideAmendmentWindow)
        )

        val extraTysErrors = Seq(
          (UNPROCESSABLE_ENTITY, "MISSING_ANNUAL_ALLOWANCE_REDUCTION", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "MISSING_TYPE_OF_REDUCTION", INTERNAL_SERVER_ERROR, InternalError),
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

    def setupStubs(): StubMapping

    def mtdRequest: WSRequest = {
      AuthStub.resetAll()
      setupStubs()
      buildRequest(s"/pensions/$nino/$taxYear")
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.3.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

  }

  private trait NonTysTest extends Test {

    def taxYear: String       = "2021-22"
    def downstreamUri: String = s"/income-tax/charges/pensions/$nino/2021-22"
  }

  private trait IfsTest extends Test {

    def taxYear: String       = "2023-24"
    def downstreamUri: String = s"/income-tax/charges/pensions/23-24/$nino"
  }

}
