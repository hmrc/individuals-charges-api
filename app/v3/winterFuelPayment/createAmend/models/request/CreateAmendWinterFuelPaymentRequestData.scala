package v3.winterFuelPayment.createAmend.models.request

import shared.models.domain.{Nino, TaxYear}

case class CreateAmendWinterFuelPaymentRequestData(nino: Nino, taxYear: TaxYear, body: CreateAmendWinterFuelPaymentRequestBody)

