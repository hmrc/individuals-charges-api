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

package v1.models.des

case class LumpSumBenefitTakenInExcessOfLifetimeAllowance(amount: BigDecimal, taxPaid: BigDecimal)

case class BenefitInExcessOfLifetimeAllowance(amount: BigDecimal, taxPaid: BigDecimal)

case class LifetimeAllowance(amount: BigDecimal, taxPaid: BigDecimal)

//scalastyle:off
//pensionSavingsTaxCharges	Object	O
//formats are the same and map to corresponding Retrieve Pension Charges Response model
//pensionSavingsTaxCharges.pensionSchemeTaxReference	Array	M
//pensionSavingsTaxCharges.lumpSumBenefitTakenInExcessOfLifetimeAllowance	Object O
//pensionSavingsTaxCharges.lumpSumBenefitTakenInExcessOfLifetimeAllowance.amount	Number	M
//pensionSavingsTaxCharges.lumpSumBenefitTakenInExcessOfLifetimeAllowance.taxPaid	Number	M
//pensionSavingsTaxCharges.benefitInExcessOfLifetimeAllowance	Object	O
//pensionSavingsTaxCharges.benefitInExcessOfLifetimeAllowance.amount Number	M
//pensionSavingsTaxCharges.benefitInExcessOfLifetimeAllowance.taxPaid	Number	M
//pensionSavingsTaxCharges.isAnnualAllowanceReduced	Boolean	O
//pensionSavingsTaxCharges.taperedAnnualAllowance	Boolean	O
//pensionSavingsTaxCharges.moneyPurchasedAllowance	Boolean	O
//
case class PensionSavingsTaxCharges(pensionSchemeTaxReference: Seq[String],
                                    lumpSumBenefitTakenInExcessOfLifetimeAllowance: Option[LifetimeAllowance],
                                    benefitInExcessOfLifetimeAllowance: Option[LifetimeAllowance],
                                    isAnnualAllowanceReduced: Option[Boolean],
                                    taperedAnnualAllowance: Option[Boolean],
                                    moneyPurchasedAllowance: Option[Boolean]
                                   )

case class OverseasSchemeProvider(providerName: String,
                                  providerAddress: String,
                                  providerCountryCode: String,
                                  qualifyingRecognisedOverseasPensionScheme: Seq[String]
                                 )

case class PensionSchemeOverseasTransfers(overseasSchemeProvider: Seq[OverseasSchemeProvider],
                                          transferCharge: BigDecimal,
                                          transferChargeTaxPaid: BigDecimal
                                         )

case class Charge(amount: BigDecimal, foreignTaxPaid: BigDecimal)

case class PensionSchemeUnauthorisedPayments(pensionSchemeTaxReference: Seq[String],
                                             surcharge: Charge,
                                             noSurcharge: Charge
                                            )

case class PensionContributions(pensionSchemeTaxReference: Seq[String],
                                inExcessOfTheAnnualAllowance: BigDecimal,
                                annualAllowanceTaxPaid: BigDecimal
                               )

  case class OverseasPensionContributions(overseasSchemeProvider: OverseasPensionContributions,
                                          shortServiceRefund: BigDecimal,
                                          shortServiceRefundTaxPaid: BigDecimal
                                         )

case class RetrievePensionChargesResponse(pensionSavingsTaxCharges: Option[PensionSavingsTaxCharges],
                                          pensionSchemeOverseasTransfers: PensionSchemeOverseasTransfers,
                                          pensionSchemeUnauthorisedPayments: PensionSchemeUnauthorisedPayments,
                                          pensionContributions: PensionContributions,
                                          overseasPensionContributions: OverseasPensionContributions
                                         ) {

}

//scalastyle:off


//pensionSchemeOverseasTransfers	Object	O
//pensionSchemeOverseasTransfers.overseasSchemeProvider	Array	M
//pensionSchemeOverseasTransfers.overseasSchemeProvider.providerName	String	M
//pensionSchemeOverseasTransfers.overseasSchemeProvider.providerAddress	String	M
//pensionSchemeOverseasTransfers.overseasSchemeProvider.providerCountryCode	String	M
//pensionSchemeOverseasTransfers.overseasSchemeProvider.qualifyingRecognisedOverseasPensionScheme	Array(String)	M
//pensionSchemeOverseasTransfers.transferCharge	Number	M
//pensionSchemeOverseasTransfers.transferChargeTaxPaid	Number	M
//
//pensionSchemeUnauthorisedPayments	Object	O
//pensionSchemeUnauthorisedPayments.pensionSchemeTaxReference	Array	M
//pensionSchemeUnauthorisedPayments.surcharge	Object	O
//pensionSchemeUnauthorisedPayments.surcharge.amount	Number	M
//pensionSchemeUnauthorisedPayments.surcharge.foreignTaxPaid	Number	M
//pensionSchemeUnauthorisedPayments.noSurcharge	Object	O
//pensionSchemeUnauthorisedPayments.noSurcharge.amount	Number	M
//pensionSchemeUnauthorisedPayments.noSurcharge.foreignTaxPaid	Number	M
//
//pensionContributions	Object	O
//pensionContributions.pensionSchemeTaxReference	Array	M
//pensionContributions.inExcessOfTheAnnualAllowance	Number	M
//pensionContributions.annualAllowanceTaxPaid	Number	M
//
//overseasPensionContributions	Object	O
//overseasPensionContributions.overseasSchemeProvider	Array	M
//overseasPensionContributions.overseasSchemeProvider.providerName	String	M
//overseasPensionContributions.overseasSchemeProvider.providerAddress	String	M
//overseasPensionContributions.overseasSchemeProvider.providerCountryCode	String	M
//overseasPensionContributions.overseasSchemeProvider.qualifyingRecognisedOverseasPensionScheme	Array	M
//overseasPensionContributions.shortServiceRefund	Number	M
//overseasPensionContributions.shortServiceRefundTaxPaid	Number	M

//scalastyle:off
//HTTP/1.1 200 OK
//CorrelationId: a1e8057e-fbbc-47a8-a8b4-78d9f015c253
//Content-Type: application/json
//{
//	"submittedOn": "2020-07-27T17:00:19Z",
//	"pensionSavingsTaxCharges": {
//		"pensionSchemeTaxReference": [
//			"00123456RA"
//		],
//		"lumpSumBenefitTakenInExcessOfLifetimeAllowance": {
//			"amount": 123.45,
//			"taxPaid": 12.45
//		},
//		"benefitInExcessOfLifetimeAllowance": {
//			"amount": 123.45,
//			"taxPaid": 12.34
//		},
//		"isAnnualAllowanceReduced": true,
//		"taperedAnnualAllowance": true,
//		"moneyPurchasedAllowance": false
//	},
//	"pensionSchemeOverseasTransfers": {
//		"overseasSchemeProvider": [
//			{
//				"providerName": "Overseas Pensions Plc",
//				"providerAddress": "111 Main Street, George Town, Grand Cayman",
//				"providerCountryCode": "ESP",
//				"qualifyingRecognisedOverseasPensionScheme": [
//					"Q123456"
//				]
//			}
//		],
//		"transferCharge": 123.45,
//		"transferChargeTaxPaid": 0
//	},
//	"pensionSchemeUnauthorisedPayments": {
//		"pensionSchemeTaxReference": [
//			"00123456RA"
//		],
//		"surcharge": {
//			"amount": 123.45,
//			"foreignTaxPaid": 123.45
//		},
//		"noSurcharge": {
//			"amount": 123.45,
//			"foreignTaxPaid": 123.45
//		}
//	},
//	"pensionContributions": {
//		"pensionSchemeTaxReference": [
//			"00123456RA"
//		],
//		"inExcessOfTheAnnualAllowance": 123.45,
//		"annualAllowanceTaxPaid": 123.45
//	},
//	"overseasPensionContributions": {
//		"overseasSchemeProvider": [
//			{
//				"providerName": "Overseas Pensions Plc",
//				"providerAddress": "111 Main Street, George Town, Grand Cayman",
//				"providerCountryCode": "ESP",
//				"pensionSchemeTaxReference": [
//					"00123456RA"
//				]
//			}
//		],
//		"shortServiceRefund": 123.45,
//		"shortServiceRefundTaxPaid": 0
//	}
//}