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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package data

import play.api.libs.json.{JsValue, Json}
import v1.models.des._
import v1.models.requestData.{AmendPensionChargesRequest, PensionCharges}

object AmendPensionChargesData {

  val pensionSavingsCharge: PensionSavingsTaxCharges = PensionSavingsTaxCharges(
    Seq("00123456RA","00123456RA"),
    Some(LifetimeAllowance(123.45, 12.45)),
    Some(LifetimeAllowance(123.45, 12.34)),
    true,
    Some(true),
    Some(false)
  )

  val overseasSchemeProvider: OverseasSchemeProvider =  OverseasSchemeProvider(
    "Overseas Pensions Plc",
    "111 Main Street, George Town, Grand Cayman",
    "ESP",
    Some(Seq("Q123456")),
    None
  )
  val pensionOverseasTransfer : PensionSchemeOverseasTransfers = PensionSchemeOverseasTransfers(
    Seq(overseasSchemeProvider),
    123.45,
    0
  )
  val pensionUnauthorisedPayments : PensionSchemeUnauthorisedPayments = PensionSchemeUnauthorisedPayments(
    Seq("00123456RA", "00123456RA"),
    Some(Charge(123.45, 123.45)),
    Some(Charge(123.45, 123.45))
  )
  val pensionContributions: PensionContributions = PensionContributions(
    Seq("00123456RA", "00123456RA"),
    123.45,
    123.45
  )
  val overseasPensionContributions : OverseasPensionContributions = OverseasPensionContributions (
    Seq(overseasSchemeProvider),
    123.45,
    0
  )
  val pensionCharges: PensionCharges = PensionCharges(
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
      |		},
      |		"isAnnualAllowanceReduced": true,
      |		"taperedAnnualAllowance": true,
      |		"moneyPurchasedAllowance": false
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
      |	}
      |}
      |""".stripMargin
  )

   val emptyJson: JsValue = Json.parse(
    """
      |{}
      |""".stripMargin
  )


}
