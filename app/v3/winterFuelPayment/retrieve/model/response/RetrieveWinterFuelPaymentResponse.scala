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

package v3.winterFuelPayment.retrieve.model.response

import play.api.libs.json.*
import api.models.domain.{MtdSourceEnum, Timestamp}
import api.models.downstream.DownstreamSourceEnum
import play.api.libs.functional.syntax.*

case class RetrieveWinterFuelPaymentResponse(submittedOn: Timestamp, source: MtdSourceEnum, winterFuelPayment: BigDecimal)

object RetrieveWinterFuelPaymentResponse {

  given Reads[RetrieveWinterFuelPaymentResponse] = (
    (JsPath \ "submittedOn").read[Timestamp] and
      (JsPath \ "source").read[DownstreamSourceEnum].map(_.toMtdEnum) and
      (JsPath \ "winterFuelPayment").read[BigDecimal]
  )(RetrieveWinterFuelPaymentResponse.apply)

  given Writes[RetrieveWinterFuelPaymentResponse] = Json.writes[RetrieveWinterFuelPaymentResponse]

}
