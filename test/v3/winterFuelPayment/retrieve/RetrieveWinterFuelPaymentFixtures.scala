/*
 * Copyright 2026 HM Revenue & Customs
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

package v3.winterFuelPayment.retrieve

import play.api.libs.json.{JsValue, Json}
import api.models.domain.MtdSourceEnum.`hmrc-held`
import api.models.domain.Timestamp
import v3.winterFuelPayment.retrieve.model.response.RetrieveWinterFuelPaymentResponse

object RetrieveWinterFuelPaymentFixtures {

  val responseModel: RetrieveWinterFuelPaymentResponse =
    RetrieveWinterFuelPaymentResponse(
      submittedOn = Timestamp("2026-07-24T14:15:22.544Z"),
      source = `hmrc-held`,
      winterFuelPayment = 210.99
    )

  val responseMtdJson: JsValue = Json.parse(
    """
      |{
      |  "submittedOn": "2026-07-24T14:15:22.544Z",
      |  "source": "hmrc-held",
      |  "winterFuelPayment": 210.99
      |}
      |""".stripMargin
  )

  val responseDownstreamJson: JsValue = Json.parse(
    """
      |{
      |	 "submittedOn": "2026-07-24T14:15:22.544Z",
      |	 "source": "HMRC-HELD",
      |	 "winterFuelPayment": 210.99
      |}
      |""".stripMargin
  )

}
