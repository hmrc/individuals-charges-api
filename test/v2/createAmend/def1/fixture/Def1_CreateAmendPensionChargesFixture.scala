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

package v2.createAmend.def1.fixture

import play.api.libs.json.{JsValue, Json}
import v2.createAmend.def1.model.request._

object Def1_CreateAmendPensionChargesFixture {

  val pensionSavingsCharge: PensionSavingsTaxCharges = PensionSavingsTaxCharges(
    pensionSchemeTaxReference = Seq("00123456RA", "00123456RA"),
    lumpSumBenefitTakenInExcessOfLifetimeAllowance = Some(LifetimeAllowance(123.45, 12.45)),
    benefitInExcessOfLifetimeAllowance = Some(LifetimeAllowance(123.45, 12.34))
  )

  val overseasSchemeProvider: OverseasSchemeProvider = OverseasSchemeProvider(
    providerName = "Overseas Pensions Plc",
    providerAddress = "111 Main Street, George Town, Grand Cayman",
    providerCountryCode = "ESP",
    qualifyingRecognisedOverseasPensionScheme = Some(Seq("Q123456")),
    pensionSchemeTaxReference = None
  )

  val pensionOverseasTransfer: PensionSchemeOverseasTransfers = PensionSchemeOverseasTransfers(
    overseasSchemeProvider = Seq(overseasSchemeProvider),
    transferCharge = 123.45,
    transferChargeTaxPaid = 0
  )

  val pensionUnauthorisedPayments: PensionSchemeUnauthorisedPayments = PensionSchemeUnauthorisedPayments(
    pensionSchemeTaxReference = Seq("00123456RA", "00123456RA"),
    surcharge = Some(Charge(123.45, 123.45)),
    noSurcharge = Some(Charge(123.45, 123.45))
  )

  val pensionContributions: PensionContributions = PensionContributions(
    pensionSchemeTaxReference = Seq("00123456RA", "00123456RA"),
    inExcessOfTheAnnualAllowance = 123.45,
    annualAllowanceTaxPaid = 123.45,
    isAnnualAllowanceReduced = Some(true),
    taperedAnnualAllowance = Some(true),
    moneyPurchasedAllowance = Some(false)
  )

  val overseasPensionContributions: OverseasPensionContributions = OverseasPensionContributions(
    overseasSchemeProvider = Seq(overseasSchemeProvider),
    shortServiceRefund = 123.45,
    shortServiceRefundTaxPaid = 0
  )

  val createAmendPensionChargesRequestBody: Def1_CreateAmendPensionChargesRequestBody = Def1_CreateAmendPensionChargesRequestBody(
    pensionSavingsTaxCharges = Some(pensionSavingsCharge),
    pensionSchemeOverseasTransfers = Some(pensionOverseasTransfer),
    pensionSchemeUnauthorisedPayments = Some(pensionUnauthorisedPayments),
    pensionContributions = Some(pensionContributions),
    overseasPensionContributions = Some(overseasPensionContributions)
  )

  val invalidJson: JsValue = Json.parse(
    """
      |{
      | "pensionSavingsTaxCharges": {"sponge":"bob"}
      |}
      |""".stripMargin
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
      |		"inExcessOfTheAnnualAllowance": 123.45,
      |		"annualAllowanceTaxPaid": 123.45,
      |		"isAnnualAllowanceReduced": true,
      |		"taperedAnnualAllowance": true,
      |		"moneyPurchasedAllowance": false
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

  val fullValidJsonUpdated: JsValue = Json.parse(
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
      |		"inExcessOfTheAnnualAllowance": 123.45,
      |		"annualAllowanceTaxPaid": 123.45,
      |		"isAnnualAllowanceReduced": true,
      |		"taperedAnnualAllowance": true,
      |		"moneyPurchasedAllowance": false
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

  val invalidNameJson: JsValue = Json.parse(
    """
      |{
      |	"pensionSchemeOverseasTransfers": {
      |		"overseasSchemeProvider": [
      |			{
      |				"providerName": "",
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
      |	"overseasPensionContributions": {
      |		"overseasSchemeProvider": [
      |			{
      |				"providerName": "r75w46tugyfsbhkfgudifijgklpd;[';g[p56097i-[-trgpfvc;l'd,km.gjn,hrtuieois9pawso0pzcjnx,bhsjfyegui7yo8w4ue9qia;oskjxcs,bhvdyuiertoiwefd",
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

  val invalidAddressJson: JsValue = Json.parse(
    """
      |{
      |	"pensionSchemeOverseasTransfers": {
      |		"overseasSchemeProvider": [
      |			{
      |				"providerName": "Bobby",
	  |     	    "providerAddress": "",
      |				"providerCountryCode": "ESP",
      |				"qualifyingRecognisedOverseasPensionScheme": [
      |					"Q123456"
      |				]
      |			}
      |		],
      |		"transferCharge": 123.45,
      |		"transferChargeTaxPaid": 0
      |	},
      |	"overseasPensionContributions": {
      |		"overseasSchemeProvider": [
      |			{
      |				"providerName": "Bobby",
      |				"providerAddress": "4334534545634563456345634563 43345345456345634563456345634334534545634563456345634563 43345345456345634563456345634334534545634563456345634563 43345345456345634563456345634334534545634563456345634563 4334534545634563456345634563 4334534545634563456345634563",
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

  val fullValidJson: JsValue = Json.parse(
    """
      |{
      |	"pensionSavingsTaxCharges": {
      |		"pensionSchemeTaxReference": [
      |			"00123456RA","00123456RA"
      |		],
      |		"lumpSumBenefitTakenInExcessOfLifetimeAllowance": {
      |			"amount": 123.45,
      |			"taxPaid": 12.45
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
      |		"inExcessOfTheAnnualAllowance": 123.45,
      |		"annualAllowanceTaxPaid": 123.45,
      |		"isAnnualAllowanceReduced": true,
      |		"taperedAnnualAllowance": true,
      |		"moneyPurchasedAllowance": false
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

  val invalidJsonWithExtraField: JsValue = Json.parse(
    """
      |{
      |	"pensionSavingsTaxChargesExtra": {
      |		"pensionSchemeTaxReference": [
      |			"00123456RA","00123456RA"
      |		],
      |		"lumpSumBenefitTakenInExcessOfLifetimeAllowance": {
      |			"amount": 123.45,
      |			"taxPaid": 12.45
      |		}
      |	},
      |	"pensionSavingsTaxCharges": {
      |		"pensionSchemeTaxReference": [
      |			"00123456RA","00123456RA"
      |		],
      |		"lumpSumBenefitTakenInExcessOfLifetimeAllowance": {
      |			"amount": 123.45,
      |			"taxPaid": 12.45
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
      |		"inExcessOfTheAnnualAllowance": 123.45,
      |		"annualAllowanceTaxPaid": 123.45,
      |		"isAnnualAllowanceReduced": true,
      |		"taperedAnnualAllowance": true,
      |		"moneyPurchasedAllowance": false
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

  val boolean1Json: JsValue = Json.parse(
    """
      |{
      |	"pensionSavingsTaxCharges": {
      |		"pensionSchemeTaxReference": [
      |			"00123456RA","00123456RA"
      |		],
      |		"benefitInExcessOfLifetimeAllowance": {
      |			"amount": 123.45,
      |			"taxPaid": 12.45
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
      |		"inExcessOfTheAnnualAllowance": 123.45,
      |		"annualAllowanceTaxPaid": 123.45,
      |		"isAnnualAllowanceReduced": true,
      |		"taperedAnnualAllowance": true,
      |		"moneyPurchasedAllowance": false
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

  val boolean2Json: JsValue = Json.parse(
    """
      |{
      |	"pensionSavingsTaxCharges": {
      |		"pensionSchemeTaxReference": [
      |			"00123456RA","00123456RA"
      |		],
      |		"lumpSumBenefitTakenInExcessOfLifetimeAllowance": {
      |			"amount": 123.45,
      |			"taxPaid": 12.45
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
      |		"inExcessOfTheAnnualAllowance": 123.45,
      |		"annualAllowanceTaxPaid": 123.45,
      |		"isAnnualAllowanceReduced": true,
      |		"taperedAnnualAllowance": true,
      |		"moneyPurchasedAllowance": false
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

  val booleans3Json: JsValue = Json.parse(
    """
      |{
      |	"pensionSavingsTaxCharges": {
      |		"pensionSchemeTaxReference": [
      |			"00123456RA","00123456RA"
      |		],
      |		"lumpSumBenefitTakenInExcessOfLifetimeAllowance": {
      |			"amount": 123.45,
      |			"taxPaid": 12.45
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
      |		"inExcessOfTheAnnualAllowance": 123.45,
      |		"annualAllowanceTaxPaid": 123.45,
      |		"isAnnualAllowanceReduced": true,
      |		"taperedAnnualAllowance": true,
      |		"moneyPurchasedAllowance": false
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

  // scalastyle:off
  def fullReferencesJson(qrop: String, pensionRef: String): JsValue = Json.parse(
    s"""
       |{
       |	"pensionSavingsTaxCharges": {
       |		"pensionSchemeTaxReference": [
       |			"00123456RA","00123456RA"
       |		],
       |		"lumpSumBenefitTakenInExcessOfLifetimeAllowance": {
       |			"amount": 123.45,
       |			"taxPaid": 12.45
       |		}
       |	},
       |	"pensionSchemeOverseasTransfers": {
       |		"overseasSchemeProvider": [
       |			{
       |				"providerName": "Overseas Pensions Plc",
       |				"providerAddress": "111 Main Street, George Town, Grand Cayman",
       |				"providerCountryCode": "ESP",
       |				"qualifyingRecognisedOverseasPensionScheme": [
       |					"$qrop"
       |				]
       |			},{
       |				"providerName": "Overseas Pensions Plc",
       |				"providerAddress": "111 Main Street, George Town, Grand Cayman",
       |				"providerCountryCode": "ESP",
       |				"pensionSchemeTaxReference": [
       |					"$pensionRef"
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
       |		"annualAllowanceTaxPaid": 123.45,
       |		"isAnnualAllowanceReduced": true,
       |		"taperedAnnualAllowance": true,
       |		"moneyPurchasedAllowance": false
       |	},
       |	"overseasPensionContributions": {
       |		"overseasSchemeProvider": [
       |			{
       |				"providerName": "Overseas Pensions Plc",
       |				"providerAddress": "111 Main Street, George Town, Grand Cayman",
       |				"providerCountryCode": "ESP",
       |				"qualifyingRecognisedOverseasPensionScheme": [
       |					"$qrop"
       |				]
       |			},{
       |				"providerName": "Overseas Pensions Plc",
       |				"providerAddress": "111 Main Street, George Town, Grand Cayman",
       |				"providerCountryCode": "ESP",
       |				"pensionSchemeTaxReference": [
       |					"$pensionRef"
       |				]
       |			}
       |		],
       |		"shortServiceRefund": 123.45,
       |		"shortServiceRefundTaxPaid": 0
       |	}
       |}
       |""".stripMargin
  )

  val fullJsonWithInvalidCountries: JsValue = Json.parse(
    """
      |{
      |	"pensionSavingsTaxCharges": {
      |		"pensionSchemeTaxReference": [
      |			"00123456RA","00123456RA"
      |		],
      |		"lumpSumBenefitTakenInExcessOfLifetimeAllowance": {
      |			"amount": 123.45,
      |			"taxPaid": 12.45
      |		}
      |	},
      |	"pensionSchemeOverseasTransfers": {
      |		"overseasSchemeProvider": [
      |			{
      |				"providerName": "Overseas Pensions Plc",
      |				"providerAddress": "111 Main Street, George Town, Grand Cayman",
      |				"providerCountryCode": "BEANS",
      |				"qualifyingRecognisedOverseasPensionScheme": [
      |					"Q123456"
      |				]
      |			},{
      |				"providerName": "Overseas Pensions Plc",
      |				"providerAddress": "111 Main Street, George Town, Grand Cayman",
      |				"providerCountryCode": "LOL",
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
      |		"annualAllowanceTaxPaid": 123.45,
      |		"isAnnualAllowanceReduced": true,
      |		"taperedAnnualAllowance": true,
      |		"moneyPurchasedAllowance": false
      |	},
      |	"overseasPensionContributions": {
      |		"overseasSchemeProvider": [
      |			{
      |				"providerName": "Overseas Pensions Plc",
      |				"providerAddress": "111 Main Street, George Town, Grand Cayman",
      |				"providerCountryCode": "LOL",
      |				"qualifyingRecognisedOverseasPensionScheme": [
      |					"Q123456"
      |				]
      |			},{
      |				"providerName": "Overseas Pensions Plc",
      |				"providerAddress": "111 Main Street, George Town, Grand Cayman",
      |				"providerCountryCode": "BEANS",
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

  // scalastyle:off
  def fullJsonWithInvalidCountryFormat(cc: String): JsValue = Json.parse(
    s"""
       |{
       |	"pensionSchemeOverseasTransfers": {
       |		"overseasSchemeProvider": [
       |			{
       |				"providerName": "Overseas Pensions Plc",
       |				"providerAddress": "111 Main Street, George Town, Grand Cayman",
       |				"providerCountryCode": "ESP",
       |				"qualifyingRecognisedOverseasPensionScheme": [
       |					"Q123456"
       |				]
       |			},{
       |				"providerName": "Overseas Pensions Plc",
       |				"providerAddress": "111 Main Street, George Town, Grand Cayman",
       |				"providerCountryCode": "ESP",
       |				"qualifyingRecognisedOverseasPensionScheme": [
       |					"Q123456"
       |				]
       |			},{
       |				"providerName": "Overseas Pensions Plc",
       |				"providerAddress": "111 Main Street, George Town, Grand Cayman",
       |				"providerCountryCode": "$cc",
       |				"qualifyingRecognisedOverseasPensionScheme": [
       |					"Q123456"
       |				]
       |			}
       |		],
       |		"transferCharge": 123.45,
       |		"transferChargeTaxPaid": 0
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
       |			},{
       |				"providerName": "Overseas Pensions Plc",
       |				"providerAddress": "111 Main Street, George Town, Grand Cayman",
       |				"providerCountryCode": "ESP",
       |				"qualifyingRecognisedOverseasPensionScheme": [
       |					"Q123456"
       |				]
       |			},{
       |				"providerName": "Overseas Pensions Plc",
       |				"providerAddress": "111 Main Street, George Town, Grand Cayman",
       |				"providerCountryCode": "$cc",
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

  // scalastyle:off
  def fullJson(bigDecimal: BigDecimal): JsValue = Json.parse(
    s"""
       |{
       |	"pensionSavingsTaxCharges": {
       |		"pensionSchemeTaxReference": [
       |			"00123456RA","00123456RA"
       |		],
       |		"lumpSumBenefitTakenInExcessOfLifetimeAllowance": {
       |			"amount": $bigDecimal,
       |			"taxPaid": $bigDecimal
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
       |		"transferCharge": $bigDecimal,
       |		"transferChargeTaxPaid": $bigDecimal
       |	},
       |	"pensionSchemeUnauthorisedPayments": {
       |		"pensionSchemeTaxReference": [
       |			"00123456RA","00123456RA"
       |		],
       |		"surcharge": {
       |			"amount": $bigDecimal,
       |			"foreignTaxPaid": $bigDecimal
       |		},
       |		"noSurcharge": {
       |			"amount": $bigDecimal,
       |			"foreignTaxPaid": $bigDecimal
       |		}
       |	},
       |	"pensionContributions": {
       |		"pensionSchemeTaxReference": [
       |			"00123456RA","00123456RA"
       |		],
       |		"inExcessOfTheAnnualAllowance": $bigDecimal,
       |		"annualAllowanceTaxPaid": $bigDecimal,
       |		"isAnnualAllowanceReduced": true,
       |		"taperedAnnualAllowance": true,
       |		"moneyPurchasedAllowance": false
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
       |		"shortServiceRefund": $bigDecimal,
       |		"shortServiceRefundTaxPaid": $bigDecimal
       |	}
       |}
       |""".stripMargin
  )

}
