package v1.controllers.requestParsers.validators.validations

import support.UnitSpec
import v1.models.errors.RulePensionReferenceError

class RulePensionReferenceValidationSpec extends UnitSpec {
  val qualifyingRecognisedOverseasPensionSchemeReferenceNumber = Seq("Q123456")
  val pensionSchemeTaxReference = Seq("00123456RA")

  "Rule Pension Reference validation" when {
    "only one provided" must {
      "return no errors for qualifyingRecognisedOverseasPensionSchemeReferenceNumber" in {
        RulePensionReferenceValidation.validate(Some(qualifyingRecognisedOverseasPensionSchemeReferenceNumber), None)shouldBe NoValidationErrors
      }
      "return no errors for pensionSchemeTaxReference" in {
        RulePensionReferenceValidation.validate(None, Some(pensionSchemeTaxReference))shouldBe NoValidationErrors
      }
    }
    "neither is provided" must {
      "return no errors" in {
        RulePensionReferenceValidation.validate(None, None)shouldBe NoValidationErrors
      }
    }
    "both are provided" must {
      "return RULE_PENSION_REFERENCE error" in {
        RulePensionReferenceValidation.validate(
          Some(qualifyingRecognisedOverseasPensionSchemeReferenceNumber),
          Some(pensionSchemeTaxReference)) shouldBe List(RulePensionReferenceError)
      }
    }
  }
}
