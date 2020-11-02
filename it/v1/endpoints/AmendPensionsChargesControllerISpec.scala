/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIED OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package v1.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import data.AmendPensionChargesData._
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import support.IntegrationBaseSpec
import v1.models.errors._
import v1.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}

class AmendPensionsChargesControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino = "AA123456A"
    val taxYear = "2021-22"

    def uri: String = s"/pensions/$nino/$taxYear"
    def desUri: String = s"/income-tax/charges/pensions/$nino/$taxYear"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }

    def errorBody(code: String): String =
      s"""
         |      {
         |        "code": "$code",
         |        "reason": "des message"
         |      }
    """.stripMargin

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
  }

  "Calling the retrieve endpoint" should {

    "return a 200 status code" when {

      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.PUT, desUri, Status.NO_CONTENT)
        }

        val response: WSResponse = await(request().put(fullValidJson))
        response.status shouldBe Status.OK
        response.json shouldBe hateoasResponse
        response.header("X-CorrelationId").nonEmpty shouldBe true
        response.header("Content-Type") shouldBe Some("application/json")
      }
      "any valid request is made with different booleans" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.PUT, desUri, Status.NO_CONTENT)
        }

        val response: WSResponse = await(request().put(boolean1Json))
        response.status shouldBe Status.OK
        response.json shouldBe hateoasResponse
        response.header("X-CorrelationId").nonEmpty shouldBe true
        response.header("Content-Type") shouldBe Some("application/json")

        val response2: WSResponse = await(request().put(boolean2Json))
        response2.status shouldBe Status.OK
        response2.json shouldBe hateoasResponse
        response2.header("X-CorrelationId").nonEmpty shouldBe true
        response2.header("Content-Type") shouldBe Some("application/json")

        val response3: WSResponse = await(request().put(booleans3Json))
        response3.status shouldBe Status.OK
        response3.json shouldBe hateoasResponse
        response3.header("X-CorrelationId").nonEmpty shouldBe true
        response3.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {

      "validation error" when {
        def validationErrorTest(requestNino: String, requestTaxYear: String, requestBody: JsValue,
                                expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error ${
            if(expectedBody.equals(TaxYearFormatError)) java.util.UUID.randomUUID else ""
          }" in new Test {

            override val nino: String = requestNino
            override val taxYear: String = requestTaxYear

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(requestNino)
            }

            val response: WSResponse = await(request().put(requestBody))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          ("Badnino", "2019-20", fullValidJson, Status.BAD_REQUEST, NinoFormatError),
          ("AA123456A", "beans", fullValidJson, Status.BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "20!0-22", fullValidJson, Status.BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "203100", fullValidJson, Status.BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "2018-19", fullValidJson, Status.BAD_REQUEST, RuleTaxYearNotSupportedError),
          ("AA123456A", "2021-22", invalidJson, Status.BAD_REQUEST, RuleIncorrectOrEmptyBodyError),
          ("AA123456A", "2021-22", invalidNameJson, Status.BAD_REQUEST, ProviderNameFormatError.copy(
            paths = Some(Seq(
              "/pensionSchemeOverseasTransfers/overseasSchemeProvider/0/providerName",
              "/overseasPensionContributions/overseasSchemeProvider/0/providerName"
            ))
          )),("AA123456A", "2021-22", invalidAddressJson, Status.BAD_REQUEST, ProviderAddressFormatError.copy(
            paths = Some(Seq(
              "/pensionSchemeOverseasTransfers/overseasSchemeProvider/0/providerAddress",
              "/overseasPensionContributions/overseasSchemeProvider/0/providerAddress"
            ))
          )),
          ("AA123456A", "2021-22", fullReferencesJson("Q123456","453"), Status.BAD_REQUEST, PensionSchemeTaxRefFormatError.copy(
            paths = Some(Seq(
            "/pensionSchemeOverseasTransfers/overseasSchemeProvider/1/pensionSchemeTaxReference/0",
            "/overseasPensionContributions/overseasSchemeProvider/1/pensionSchemeTaxReference/0"
            ))
          )),
          ("AA123456A", "2021-22", fullReferencesJson("234","00123456RA"), Status.BAD_REQUEST, QOPSRefFormatError.copy(
            paths = Some(Seq(
            "/pensionSchemeOverseasTransfers/overseasSchemeProvider/0/qualifyingRecognisedOverseasPensionScheme/0",
            "/overseasPensionContributions/overseasSchemeProvider/0/qualifyingRecognisedOverseasPensionScheme/0"
            ))
          )),
          ("AA123456A", "2021-22", fullJsonWithInvalidCountryFormat("1YM"), Status.BAD_REQUEST, RuleCountryCodeError.copy(
            paths = Some(Seq(
              "/pensionSchemeOverseasTransfers/overseasSchemeProvider/2/providerCountryCode",
              "/overseasPensionContributions/overseasSchemeProvider/2/providerCountryCode"
            ))
          )),
          ("AA123456A", "2021-22", fullJsonWithInvalidCountryFormat("INVALID"), Status.BAD_REQUEST, CountryCodeFormatError.copy(paths = Some(
            Seq(
              "/pensionSchemeOverseasTransfers/overseasSchemeProvider/2/providerCountryCode",
              "/overseasPensionContributions/overseasSchemeProvider/2/providerCountryCode"
            )
          ))
          ),
          ("AA123456A", "2021-22", fullJson(999999999999.99), Status.BAD_REQUEST, ValueFormatError.copy(
            paths = Some(Seq(
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
            ))
          ))
        )

        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "des service error" when {
        def serviceErrorTest(desStatus: Int, desCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"des returns an $desCode error and status $desStatus" in new Test {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DesStub.onError(DesStub.PUT, desUri, desStatus, errorBody(desCode))
            }

            val response: WSResponse = await(request().put(fullValidJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          (Status.NOT_FOUND, "INVALID_TAXABLE_ENTITY_ID", Status.NOT_FOUND, NotFoundError),
          (Status.BAD_REQUEST, "INVALID_TAX_YEAR", Status.BAD_REQUEST, TaxYearFormatError),
          (Status.BAD_REQUEST, "INVALID_CORRELATIONID", Status.INTERNAL_SERVER_ERROR, DownstreamError),
          (Status.BAD_REQUEST, "INVALID_PAYLOAD", Status.BAD_REQUEST, RuleIncorrectOrEmptyBodyError),
          (Status.INTERNAL_SERVER_ERROR, "SERVER_ERROR", Status.INTERNAL_SERVER_ERROR, DownstreamError),
          (Status.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", Status.INTERNAL_SERVER_ERROR, DownstreamError)
        )

        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }
}
