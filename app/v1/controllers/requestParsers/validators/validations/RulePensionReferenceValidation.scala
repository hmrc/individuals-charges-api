package v1.controllers.requestParsers.validators.validations

import v1.models.errors.{MtdError,RulePensionReferenceError}
object RulePensionReferenceValidation {
  def validate(qualifyingRecognisedOverseasPensionSchemeReferenceNumber: Option[Seq[String]],
               pensionSchemeTaxReference: Option[Seq[String]]): List[MtdError] =
    (qualifyingRecognisedOverseasPensionSchemeReferenceNumber,pensionSchemeTaxReference) match {
      case (Some(_),Some(_)) => List(RulePensionReferenceError)
      case _ => NoValidationErrors
    }
}
