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

package v2.data

import anyVersion.models.response.retrievePensionCharges._
import play.api.libs.json.{JsValue, Json}
import v2.models.response.retrievePensionCharges.{PensionContributions, PensionSavingsTaxCharges, RetrievePensionChargesResponse}

object RetrievePensionChargesData {

  val pensionSavingsCharge: PensionSavingsTaxCharges = PensionSavingsTaxCharges(
    Seq("00123456RA", "00123456RA"),
    Some(LifetimeAllowance(123.45, 12.45)),
    Some(LifetimeAllowance(123.45, 12.34))
  )

  val overseasSchemeProvider: OverseasSchemeProvider = OverseasSchemeProvider(
    "Overseas Pensions Plc",
    "111 Main Street, George Town, Grand Cayman",
    "ESP",
    Some(Seq("Q123456")),
    None
  )

  val pensionOverseasTransfer: PensionSchemeOverseasTransfers = PensionSchemeOverseasTransfers(
    Seq(overseasSchemeProvider),
    123.45,
    0
  )

  val pensionUnauthorisedPayments: PensionSchemeUnauthorisedPayments = PensionSchemeUnauthorisedPayments(
    Seq("00123456RA", "00123456RA"),
    Some(Charge(123.45, 123.45)),
    Some(Charge(123.45, 123.45))
  )

  val pensionContributions: PensionContributions = PensionContributions(
    Seq("00123456RA", "00123456RA"),
    Some(true),
    Some(true),
    Some(false),
    123.45,
    123.45
  )

  val overseasPensionContributions: OverseasPensionContributions = OverseasPensionContributions(
    Seq(overseasSchemeProvider),
    123.45,
    0
  )

  val retrieveResponse: RetrievePensionChargesResponse = RetrievePensionChargesResponse(
    Some(pensionSavingsCharge),
    Some(pensionOverseasTransfer),
    Some(pensionUnauthorisedPayments),
    Some(pensionContributions),
    Some(overseasPensionContributions)
  )

  val fullJson: JsValue = Json.parse(
    """
      |{
      |	"pensionSavingsTaxCharges": {
      |		"pensionSchemeTaxReference": [
      |			"00123456RA","00123456RA"
      |		],
      |		"lumpSumBenefitTakenInExcessOfLifetimeAllowance": {
      |			"amount": 123.45,
      |			"taxPaid": 12.45
      |		},
      |		"benefitInExcessOfLifetimeAllowance": {
      |			"amount": 123.45,
      |			"taxPaid": 12.34
      |		}
      |	},
      |	"pensionSchemeOverseasTransfers": {
      |		"overseasSchemeProvider": [
      |			{
      |				"providerName": "Overseas Pensions Plc",
      |				"providerAddress": "111 Main Street, George Town, Grand Cayman",
      |				"providerCountryCode": "ESP",
      |				"qualifyingRecognisedOverseasPensionScheme": [
      |					"Q123456"
      |				]
      |			}
      |		],
      |		"transferCharge": 123.45,
      |		"transferChargeTaxPaid": 0
      |	},
      |	"pensionSchemeUnauthorisedPayments": {
      |		"pensionSchemeTaxReference": [
      |			"00123456RA","00123456RA"
      |		],
      |		"surcharge": {
      |			"amount": 123.45,
      |			"foreignTaxPaid": 123.45
      |		},
      |		"noSurcharge": {
      |			"amount": 123.45,
      |			"foreignTaxPaid": 123.45
      |		}
      |	},
      |	"pensionContributions": {
      |		"pensionSchemeTaxReference": [
      |			"00123456RA","00123456RA"
      |		],
      |     "isAnnualAllowanceReduced": true,
      |     "taperedAnnualAllowance": true,
      |     "moneyPurchasedAllowance": false,
      |		"inExcessOfTheAnnualAllowance": 123.45,
      |		"annualAllowanceTaxPaid": 123.45
      |	},
      |	"overseasPensionContributions": {
      |		"overseasSchemeProvider": [
      |			{
      |				"providerName": "Overseas Pensions Plc",
      |				"providerAddress": "111 Main Street, George Town, Grand Cayman",
      |				"providerCountryCode": "ESP",
      |				"qualifyingRecognisedOverseasPensionScheme": [
      |					"Q123456"
      |				]
      |			}
      |		],
      |		"shortServiceRefund": 123.45,
      |		"shortServiceRefundTaxPaid": 0
      |	}
      |}
      |""".stripMargin
  )

  def fullJsonWithHateoas(taxYear: String): JsValue = Json.parse(
    s"""
      |{
      |	"pensionSavingsTaxCharges": {
      |		"pensionSchemeTaxReference": [
      |			"00123456RA","00123456RA"
      |		],
      |		"lumpSumBenefitTakenInExcessOfLifetimeAllowance": {
      |			"amount": 123.45,
      |			"taxPaid": 12.45
      |		},
      |		"benefitInExcessOfLifetimeAllowance": {
      |			"amount": 123.45,
      |			"taxPaid": 12.34
      |		},
      |     "isAnnualAllowanceReduced": true,
      |     "taperedAnnualAllowance": true,
      |     "moneyPurchasedAllowance": false
      |	},
      |	"pensionSchemeOverseasTransfers": {
      |		"overseasSchemeProvider": [
      |			{
      |				"providerName": "Overseas Pensions Plc",
      |				"providerAddress": "111 Main Street, George Town, Grand Cayman",
      |				"providerCountryCode": "ESP",
      |				"qualifyingRecognisedOverseasPensionScheme": [
      |					"Q123456"
      |				]
      |			}
      |		],
      |		"transferCharge": 123.45,
      |		"transferChargeTaxPaid": 0
      |	},
      |	"pensionSchemeUnauthorisedPayments": {
      |		"pensionSchemeTaxReference": [
      |			"00123456RA","00123456RA"
      |		],
      |		"surcharge": {
      |			"amount": 123.45,
      |			"foreignTaxPaid": 123.45
      |		},
      |		"noSurcharge": {
      |			"amount": 123.45,
      |			"foreignTaxPaid": 123.45
      |		}
      |	},
      |	"pensionContributions": {
      |		"pensionSchemeTaxReference": [
      |			"00123456RA","00123456RA"
      |		],
      |		"inExcessOfTheAnnualAllowance": 123.45,
      |		"annualAllowanceTaxPaid": 123.45
      |	},
      |	"overseasPensionContributions": {
      |		"overseasSchemeProvider": [
      |			{
      |				"providerName": "Overseas Pensions Plc",
      |				"providerAddress": "111 Main Street, George Town, Grand Cayman",
      |				"providerCountryCode": "ESP",
      |				"qualifyingRecognisedOverseasPensionScheme": [
      |					"Q123456"
      |				]
      |			}
      |		],
      |		"shortServiceRefund": 123.45,
      |		"shortServiceRefundTaxPaid": 0
      |	},
      | "links":[
      |    {
      |      "href":"/individuals/charges/pensions/AA123456A/$taxYear",
      |      "method":"GET",
      |      "rel":"self"
      |    },
      |     {
      |      "href":"/individuals/charges/pensions/AA123456A/$taxYear",
      |      "method":"PUT",
      |      "rel":"create-and-amend-charges-pensions"
      |    },
      |    {
      |      "href":"/individuals/charges/pensions/AA123456A/$taxYear",
      |      "method":"DELETE",
      |      "rel":"delete-charges-pensions"
      |    }
      |    ]
      |}
      |""".stripMargin
  )

}
