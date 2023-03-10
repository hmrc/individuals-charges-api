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

package v2.models.response.retrievePensionCharges

import anyVersion.models.response.retrievePensionCharges._
import api.models.hateoas.Link
import api.models.hateoas.Method._
import mocks.MockAppConfig
import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class RetrievePensionChargesResponseSpec extends UnitSpec with MockAppConfig {

  val responseModel: RetrievePensionChargesResponse = RetrievePensionChargesResponse(
    Some(
      PensionSavingsTaxCharges(
        Some(Seq("00123456RA")),
        Some(LifetimeAllowance(123.12, 123.12)),
        Some(LifetimeAllowance(123.12, 123.12))
      )),
    Some(
      PensionSchemeOverseasTransfers(
        Seq(
          OverseasSchemeProvider(
            "name",
            "address",
            "postcode",
            Some(Seq("Q123456")),
            None
          )),
        123.12,
        123.12)),
    Some(
      PensionSchemeUnauthorisedPayments(
        Some(Seq("00123456RA", "00123456RA")),
        Some(Charge(123.12, 123.12)),
        Some(Charge(123.12, 123.12))
      )),
    Some(PensionContributions(Seq("00123456RA", "00123456RA"), Some(true), Some(true), Some(true), 123.12, 123.12)),
    Some(
      OverseasPensionContributions(
        Seq(
          OverseasSchemeProvider(
            "Overseas Pensions Plc",
            "111 Main Street, George Town, Grand Cayman",
            "CYM",
            Some(Seq("Q123456")),
            None
          )),
        123.12,
        123.12
      ))
  )

  val responseJson: JsValue = Json.parse("""
      |{
      |   "pensionSavingsTaxCharges": {
      |      "pensionSchemeTaxReference": ["00123456RA"],
      |      "lumpSumBenefitTakenInExcessOfLifetimeAllowance":
      |         {
      |            "amount":123.12,
      |            "taxPaid":123.12
      |         },
      |      "benefitInExcessOfLifetimeAllowance":
      |         {
      |            "amount":123.12,
      |            "taxPaid":123.12
      |         }
      |   },
      |   "pensionSchemeOverseasTransfers": {
      |     "overseasSchemeProvider": [
      |       {
      |         "providerName": "name",
      |         "providerAddress": "address",
      |         "providerCountryCode": "postcode",
      |         "qualifyingRecognisedOverseasPensionScheme": [
      |              "Q123456"
      |         ]
      |       }
      |     ],
      |     "transferCharge": 123.12,
      |     "transferChargeTaxPaid": 123.12
      |   },
      |   "pensionSchemeUnauthorisedPayments": {
      |     "pensionSchemeTaxReference": ["00123456RA", "00123456RA"],
      |     "surcharge": {
      |         "amount": 123.12,
      |         "foreignTaxPaid": 123.12
      |       },
      |     "noSurcharge": {
      |         "amount": 123.12,
      |         "foreignTaxPaid": 123.12
      |       }
      |   },
      |   "pensionContributions": {
      |     "pensionSchemeTaxReference": ["00123456RA", "00123456RA"],
      |      "isAnnualAllowanceReduced": true,
      |      "taperedAnnualAllowance": true,
      |      "moneyPurchasedAllowance": true,
      |     "inExcessOfTheAnnualAllowance": 123.12,
      |     "annualAllowanceTaxPaid": 123.12
      |   },
      |   "overseasPensionContributions": {
      |    "overseasSchemeProvider": [
      |      {
      |        "providerName": "Overseas Pensions Plc",
      |        "providerAddress": "111 Main Street, George Town, Grand Cayman",
      |        "providerCountryCode": "CYM",
      |        "qualifyingRecognisedOverseasPensionScheme": [
      |          "Q123456"
      |        ]
      |      }
      |    ],
      |    "shortServiceRefund": 123.12,
      |    "shortServiceRefundTaxPaid": 123.12
      |  }
      |}
      |""".stripMargin)

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        responseModel shouldBe responseJson.as[RetrievePensionChargesResponse]
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(responseModel) shouldBe responseJson
      }
    }
  }

  "LinksFactory" should {
    "return the correct links" in {
      val nino    = "mynino"
      val taxYear = "2017-18"

      MockAppConfig.apiGatewayContext.returns("my/context").anyNumberOfTimes
      RetrievePensionChargesResponse.RetrievePensionChargesLinksFactory.links(
        mockAppConfig,
        RetrievePensionChargesHateoasData(nino, taxYear)) shouldBe
        Seq(
          Link(s"/my/context/pensions/$nino/$taxYear", GET, "self"),
          Link(s"/my/context/pensions/$nino/$taxYear", PUT, "create-and-amend-charges-pensions"),
          Link(s"/my/context/pensions/$nino/$taxYear", DELETE, "delete-charges-pensions")
        )
    }
  }

}