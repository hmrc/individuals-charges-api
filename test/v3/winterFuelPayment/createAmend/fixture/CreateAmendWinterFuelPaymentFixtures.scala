package v3.winterFuelPayment.createAmend.fixture

import play.api.libs.json.{JsValue, Json}
import v3.winterFuelPayment.createAmend.models.request.CreateAmendWinterFuelPaymentRequestBody

object CreateAmendWinterFuelPaymentFixtures {

  val requestBodyModel: CreateAmendWinterFuelPaymentRequestBody =
    CreateAmendWinterFuelPaymentRequestBody(
      winterFuelPayment = 111.22
    )

  val validRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |  "winterFuelPayment": 111.22
      |}
      """.stripMargin
  )

}
