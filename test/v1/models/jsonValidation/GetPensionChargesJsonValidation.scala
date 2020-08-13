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

package v1.models.jsonValidation

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.github.fge.jsonschema.core.report.ProcessingReport
import com.github.fge.jsonschema.main.{JsonSchemaFactory, JsonValidator}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContentAsJson
import play.api.test.FakeRequest
import support.UnitSpec
import v1.models.des._

class GetPensionChargesJsonValidation extends UnitSpec {

  def isValidateJsonAccordingToJsonSchema(inputDoc: AnyContentAsJson, schemaDoc: String): Boolean = {
    try {
      val schemaFileInputStream = getClass.getResourceAsStream(schemaDoc)
      val schemaString = scala.io.Source.fromInputStream(schemaFileInputStream).getLines().mkString("\n")
      val mapper: ObjectMapper = new ObjectMapper()
      val inputJson: JsonNode = mapper.readTree(inputDoc.asJson.get.toString())
      val jsonSchema: JsonNode = mapper.readTree(schemaString)
      val factory = JsonSchemaFactory.byDefault()
      val validator: JsonValidator = factory.getValidator
      val report: ProcessingReport = validator.validate(jsonSchema, inputJson)
      report.isSuccess
    } catch {
      case e: Exception => false
    }
  }

  val onlySubmittedDateJson: JsValue = Json.parse(
    """{
      |"submittedOn": "2020-07-27T17:00:19Z"
      |}""".stripMargin)

  val fullJson: JsValue = Json.parse(
    """
      |{
      |	"submittedOn": "2020-07-27T17:00:19Z",
      |	"pensionSavingsTaxCharges": {
      |		"pensionSchemeTaxReference": [
      |			"00123456RA"
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
      |			"00123456RA"
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
      |			"00123456RA"
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
      |				"pensionSchemeTaxReference": [
      |					"00123456RA"
      |				]
      |			}
      |		],
      |		"shortServiceRefund": 123.45,
      |		"shortServiceRefundTaxPaid": 0
      |	}
      |}
      |""".stripMargin
  )
  val fullJsonWithArraysSwapped: JsValue = Json.parse(
    """
      |{
      |	"submittedOn": "2020-07-27T17:00:19Z",
      |	"pensionSavingsTaxCharges": {
      |		"pensionSchemeTaxReference": [
      |			"00123456RA"
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
      |       "pensionSchemeTaxReference": [
      |					"00123456RA"
      |				]
      |			}
      |		],
      |		"transferCharge": 123.45,
      |		"transferChargeTaxPaid": 0
      |	},
      |	"pensionSchemeUnauthorisedPayments": {
      |		"pensionSchemeTaxReference": [
      |			"00123456RA"
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
      |			"00123456RA"
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
      |         "Q123456"
      |       ]
      |			}
      |		],
      |		"shortServiceRefund": 123.45,
      |		"shortServiceRefundTaxPaid": 0
      |	}
      |}
      |""".stripMargin
  )

  val pensionSavingsTaxChargesJson: JsValue = Json.parse(
    """
      |{
      |	"submittedOn": "2020-07-27T17:00:19Z",
      |	"pensionSavingsTaxCharges": {
      |		"pensionSchemeTaxReference": [
      |			"00123456RA"
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
      |	}
      |}
      |""".stripMargin
  )

  val lumpSumPensionSavingsTaxChargesJson: JsValue = Json.parse(
    """
      |{
      |	"submittedOn": "2020-07-27T17:00:19Z",
      |	"pensionSavingsTaxCharges": {
      |		"pensionSchemeTaxReference": [
      |			"00123456RA"
      |		],
      |		"lumpSumBenefitTakenInExcessOfLifetimeAllowance": {
      |			"amount": 123.45,
      |			"taxPaid": 12.45
      |		},
      |		"isAnnualAllowanceReduced": true,
      |		"taperedAnnualAllowance": true,
      |		"moneyPurchasedAllowance": false
      |	}
      |}
      |""".stripMargin
  )

  val benefitInExcessPensionSavingsTaxChargesJson: JsValue = Json.parse(
    """
      |{
      |	"submittedOn": "2020-07-27T17:00:19Z",
      |	"pensionSavingsTaxCharges": {
      |		"pensionSchemeTaxReference": [
      |			"00123456RA"
      |		],
      |		"benefitInExcessOfLifetimeAllowance": {
      |			"amount": 123.45,
      |			"taxPaid": 12.34
      |		},
      |		"isAnnualAllowanceReduced": true,
      |		"taperedAnnualAllowance": true,
      |		"moneyPurchasedAllowance": false
      |	}
      |}
      |""".stripMargin
  )

  val barebonesPensionSavingsTaxChargesJson: JsValue = Json.parse(
    """
      |{
      |	"submittedOn": "2020-07-27T17:00:19Z",
      |	"pensionSavingsTaxCharges": {
      |		"pensionSchemeTaxReference": [
      |			"00123456RA"
      |		],
      |  "isAnnualAllowanceReduced": false
      |	}
      |}
      |""".stripMargin
  )

  val booleans1PensionSavingsTaxChargesJson: JsValue = Json.parse(
    """
      |{
      |	"submittedOn": "2020-07-27T17:00:19Z",
      |	"pensionSavingsTaxCharges": {
      |		"pensionSchemeTaxReference": [
      |			"00123456RA"
      |		],
      |  "isAnnualAllowanceReduced": true,
      |   "taperedAnnualAllowance": false,
      |   "moneyPurchasedAllowance": true
      |	}
      |}
      |""".stripMargin
  )

  val booleans2PensionSavingsTaxChargesJson: JsValue = Json.parse(
    """
      |{
      |	"submittedOn": "2020-07-27T17:00:19Z",
      |	"pensionSavingsTaxCharges": {
      |		"pensionSchemeTaxReference": [
      |			"00123456RA"
      |		],
      |  "isAnnualAllowanceReduced": true,
      |   "taperedAnnualAllowance": true,
      |   "moneyPurchasedAllowance": false
      |	}
      |}
      |""".stripMargin
  )

  val pensionSavingsTaxChargesPensionSchemeOverseasTransfersJson: JsValue = Json.parse(
    """
      |{
      |	"submittedOn": "2020-07-27T17:00:19Z",
      |	"pensionSavingsTaxCharges": {
      |		"pensionSchemeTaxReference": [
      |			"00123456RA"
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
      |	}
      |}
      |""".stripMargin
  )

  val partialJson1: JsValue = Json.parse(
    """
      |{
      |	"submittedOn": "2020-07-27T17:00:19Z",
      |	"pensionSavingsTaxCharges": {
      |		"pensionSchemeTaxReference": [
      |			"00123456RA"
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
      |			"00123456RA"
      |		],
      |		"surcharge": {
      |			"amount": 123.45,
      |			"foreignTaxPaid": 123.45
      |		},
      |		"noSurcharge": {
      |			"amount": 123.45,
      |			"foreignTaxPaid": 123.45
      |		}
      |	}
      |}
      |""".stripMargin
  )

  val partialJson2: JsValue = Json.parse(
    """
      |{
      |	"submittedOn": "2020-07-27T17:00:19Z",
      |	"pensionSavingsTaxCharges": {
      |		"pensionSchemeTaxReference": [
      |			"00123456RA"
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
      |			"00123456RA"
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
      |			"00123456RA"
      |		],
      |		"inExcessOfTheAnnualAllowance": 123.45,
      |		"annualAllowanceTaxPaid": 123.45
      |	}
      |}
      |""".stripMargin
  )

  val invalidJson: JsValue = Json.parse(
    """
      |{
      |	"submittedOn": "2020-07-27T17:00:19Z",
      |	"pensionSavingsTaxCharges": {
      |	}
      |}
      |""".stripMargin
  )

  val invalidJsonNoBooleans: JsValue = Json.parse(
    """
      |{
      |	"submittedOn": "2020-07-27T17:00:19Z",
      |	"pensionSavingsTaxCharges": {
      |		"pensionSchemeTaxReference": [
      |			"00123456RA"
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
      |			"00123456RA"
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
      |			"00123456RA"
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
      |				"pensionSchemeTaxReference": [
      |					"00123456RA"
      |				]
      |			}
      |		],
      |		"shortServiceRefund": 123.45,
      |		"shortServiceRefundTaxPaid": 0
      |	}
      |}
      |""".stripMargin
  )

  val invalidBooleansSupplied1: JsValue = Json.parse(
    """
      |{
      |	"submittedOn": "2020-07-27T17:00:19Z",
      |	"pensionSavingsTaxCharges": {
      |		"pensionSchemeTaxReference": [
      |			"00123456RA"
      |		],
      |  "isAnnualAllowanceReduced": true
      |	}
      |}
      |""".stripMargin
  )

  private case class JsonTest(name: String, json: JsValue, outcome: Boolean)

  "The json Schema file for Get Pension Charges Response" should {
    val schemaJsonDoc = "/desJsonSchemas/retrieve_pension_charges_response.json"

    val jsonSchemaTests: List[JsonTest] = List(
      JsonTest("only submitted date json", onlySubmittedDateJson, true),
      JsonTest("full json example", fullJson, true),
      JsonTest("full json with differing arrays used example", fullJsonWithArraysSwapped, true),
      JsonTest("only pensionSavingsTaxCharges example", pensionSavingsTaxChargesJson, true),
      JsonTest("only barebones pensionSavingsTaxCharges example", barebonesPensionSavingsTaxChargesJson, true),
      JsonTest("pensionSavingsTaxCharges first booleans example", booleans1PensionSavingsTaxChargesJson, true),
      JsonTest("pensionSavingsTaxCharges second booleans example", booleans2PensionSavingsTaxChargesJson, true),
      JsonTest("pensionSavingsTaxCharges lump sum example", lumpSumPensionSavingsTaxChargesJson, true),
      JsonTest("pensionSavingsTaxCharges benefit in excess example", benefitInExcessPensionSavingsTaxChargesJson, true),
      JsonTest("pensionSavingsTaxCharges pensionSchemeOverseasTransfers example", pensionSavingsTaxChargesPensionSchemeOverseasTransfersJson, true),
      JsonTest("pensionSavingsTaxCharges pensionSchemeOverseasTransfers pensionSchemeUnauthorisedPayments example", partialJson1, true),
      JsonTest("pensionSavingsTaxCharges pensionSchemeOverseasTransfers pensionSchemeUnauthorisedPayments pensionContributions example", partialJson2, true),
      JsonTest("invalid json", invalidJson, false),
      JsonTest("invalid json no boolean fields supplied", invalidJsonNoBooleans, false),
      JsonTest("invalid boolean fields supplied", invalidBooleansSupplied1, false)
    )

    jsonSchemaTests.foreach {
      jsonTest =>
        s"correctly validate the json for scenario - ${jsonTest.name}" in {
          isValidateJsonAccordingToJsonSchema(FakeRequest().withJsonBody(jsonTest.json).body, schemaJsonDoc) shouldBe jsonTest.outcome

          if(jsonTest.outcome){
            jsonTest.json.asOpt[RetrievePensionChargesResponse].isDefined shouldBe true
          }
        }
    }
  }

  "A json with no booleans" should {

    "read the json and default them to false" in {
      val result = invalidJsonNoBooleans.asOpt[RetrievePensionChargesResponse]

      result.isDefined shouldBe true
      result.get shouldBe RetrievePensionChargesResponse(
        Some(PensionSavingsTaxCharges(
          Seq("00123456RA"),
          Some(LifetimeAllowance(123.45,12.45)),
          Some(LifetimeAllowance(123.45,12.34)),
          false,
          false,
          false
        )),
        Some(PensionSchemeOverseasTransfers(
          Seq(OverseasSchemeProvider(
            "Overseas Pensions Plc",
            "111 Main Street, George Town, Grand Cayman",
            "ESP",
            Some(Seq("Q123456")),
            None
          )),
          123.45,
          0
        )),
        Some(PensionSchemeUnauthorisedPayments(
          Seq("00123456RA"),
          Some(Charge(123.45,123.45)),
          Some(Charge(123.45,123.45))
        )),
        Some(PensionContributions(
          Seq("00123456RA"),
          123.45,
          123.45
        )),
        Some(OverseasPensionContributions(
          Seq(OverseasSchemeProvider(
            "Overseas Pensions Plc",
            "111 Main Street, George Town, Grand Cayman",
            "ESP",
            None,
            Some(Seq("00123456RA")),
          )),
          123.45,
          0
        ))
      )
    }
  }
}
