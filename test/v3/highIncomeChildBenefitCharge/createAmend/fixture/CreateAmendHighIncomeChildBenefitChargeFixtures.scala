/*
 * Copyright 2025 HM Revenue & Customs
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

package v3.highIncomeChildBenefitCharge.createAmend.fixture

import play.api.libs.json.{JsValue, Json}
import v3.highIncomeChildBenefitCharge.createAmend.models.request.CreateAmendHighIncomeChildBenefitChargeRequestBody

object CreateAmendHighIncomeChildBenefitChargeFixtures {

  val fullRequestBodyModel: CreateAmendHighIncomeChildBenefitChargeRequestBody =
    CreateAmendHighIncomeChildBenefitChargeRequestBody(
      amountOfChildBenefitReceived = 1111.22,
      numberOfChildren = 2,
      dateCeased = Some("2025-05-08")
    )

  val minimumRequestBodyModel: CreateAmendHighIncomeChildBenefitChargeRequestBody =
    CreateAmendHighIncomeChildBenefitChargeRequestBody(
      amountOfChildBenefitReceived = 1111.22,
      numberOfChildren = 2,
      dateCeased = None
    )

  val validFullRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |  "amountOfChildBenefitReceived": 1111.22,
      |  "numberOfChildren": 2,
      |  "dateCeased": "2025-05-08"
      |}
    """.stripMargin
  )

  val validMinimumRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |  "amountOfChildBenefitReceived": 1111.22,
      |  "numberOfChildren": 2
      |}
    """.stripMargin
  )

}
