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

package v1.models.response.retrievePensionCharges

import anyVersion.models.response.retrievePensionCharges._
import api.hateoas
import api.hateoas.Method._
import mocks.MockAppConfig
import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v1.data.RetrievePensionChargesData._

class RetrievePensionChargesResponseSpec extends UnitSpec with MockAppConfig {

  val responseModel: RetrievePensionChargesResponse = RetrievePensionChargesResponse(
    Some(
      PensionSavingsTaxCharges(
        Seq("00123456RA"),
        Some(LifetimeAllowance(123.12, 123.12)),
        Some(LifetimeAllowance(123.12, 123.12)),
        isAnnualAllowanceReduced = Some(true),
        Some(true),
        Some(true)
      )
    ),
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
        Seq("00123456RA", "00123456RA"),
        Some(Charge(123.12, 123.12)),
        Some(Charge(123.12, 123.12))
      )),
    Some(PensionContributions(Seq("00123456RA", "00123456RA"), 123.12, 123.12, None, None, None)),
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
      |         },
      |      "isAnnualAllowanceReduced": true,
      |      "taperedAnnualAllowance": true,
      |      "moneyPurchasedAllowance": true
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

      MockAppConfig.apiGatewayContext.returns("my/context").anyNumberOfTimes()
      RetrievePensionChargesResponse.RetrievePensionChargesLinksFactory.links(
        mockAppConfig,
        RetrievePensionChargesHateoasData(nino, taxYear)) shouldBe
        Seq(
          hateoas.Link(s"/my/context/pensions/$nino/$taxYear", GET, "self"),
          hateoas.Link(s"/my/context/pensions/$nino/$taxYear", PUT, "create-and-amend-charges-pensions"),
          hateoas.Link(s"/my/context/pensions/$nino/$taxYear", DELETE, "delete-charges-pensions")
        )
    }
  }

  "Cl102 Changes" when {

    "isIsAnnualAllowanceReducedMissing" when {
      "missing from pensionSavingsTaxCharges" should {
        "return true" in {
          retrieveResponseCl102Fields(
            pensionSavingsChargeWithoutCl102Fields,
            pensionContributionsWithoutCl102Fields).isIsAnnualAllowanceReducedMissing shouldBe true
        }
      }

      "present in pensionSavingsTaxCharges" should {
        "return false" in {
          retrieveResponseCl102Fields(
            pensionSavingsChargeWithCl102Fields,
            pensionContributionsWithoutCl102Fields).isIsAnnualAllowanceReducedMissing shouldBe false
        }
      }
    }

    "removeFieldsFromPensionContributions" should {
      "removes fields successfully" in {
        retrieveResponseCl102FieldsInPensionContributions.removeFieldsFromPensionContributions shouldBe retrieveResponseCl102Fields(
          pensionSavingsChargeWithoutCl102Fields,
          pensionContributionsWithoutCl102Fields)
      }
    }

    "addFieldsFromPensionContributionsToPensionSavingsTaxCharges" when {
      "CL102 fields exist in pensionContributions and pensionSavingsTaxCharges exist" should {
        "successfully add cl102 fields to pensionSavingsTaxCharges" in {
          retrieveResponseCl102FieldsInPensionContributions.addFieldsFromPensionContributionsToPensionSavingsTaxCharges shouldBe Some(
            retrieveResponseCl102Fields(pensionSavingsChargeWithCl102Fields, pensionContributionsWithCl102Fields))
        }
      }
      "CL102 fields exist in pensionContributions but pensionSavingsTaxCharges does not exist" should {
        "return None" in {
          val response: RetrievePensionChargesResponse = RetrievePensionChargesResponse(
            None,
            Some(pensionOverseasTransfer),
            Some(pensionUnauthorisedPayments),
            Some(pensionContributionsWithCl102Fields),
            Some(overseasPensionContributions)
          )
          response.addFieldsFromPensionContributionsToPensionSavingsTaxCharges shouldBe None
        }
      }

      "CL102 fields do not exist in pensionContributions and pensionSavingsTaxCharges does not exist" should {
        "return response as is" in {
          val response: RetrievePensionChargesResponse = RetrievePensionChargesResponse(
            None,
            Some(pensionOverseasTransfer),
            Some(pensionUnauthorisedPayments),
            Some(pensionContributionsWithoutCl102Fields),
            Some(overseasPensionContributions)
          )
          response.addFieldsFromPensionContributionsToPensionSavingsTaxCharges shouldBe Some(response)
        }
      }

      "pensionContributions does not exist and pensionSavingsTaxCharges does exist" should {
        "return response as is" in {
          val response: RetrievePensionChargesResponse = RetrievePensionChargesResponse(
            Some(pensionSavingsChargeWithoutCl102Fields),
            Some(pensionOverseasTransfer),
            Some(pensionUnauthorisedPayments),
            None,
            Some(overseasPensionContributions)
          )
          response.addFieldsFromPensionContributionsToPensionSavingsTaxCharges shouldBe Some(response)
        }
      }

      "pensionContributions and pensionSavingsTaxCharges does not exist" should {
        "return response as is" in {
          val response: RetrievePensionChargesResponse = RetrievePensionChargesResponse(
            None,
            Some(pensionOverseasTransfer),
            Some(pensionUnauthorisedPayments),
            None,
            Some(overseasPensionContributions)
          )
          response.addFieldsFromPensionContributionsToPensionSavingsTaxCharges shouldBe Some(response)
        }
      }

    }

  }

}
