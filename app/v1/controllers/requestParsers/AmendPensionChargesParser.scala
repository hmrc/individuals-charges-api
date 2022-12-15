/*
 * Copyright 2022 HM Revenue & Customs
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

package v1.controllers.requestParsers

import play.api.libs.json._
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites

import javax.inject.Inject
import v1.controllers.requestParsers.validators.AmendPensionChargesValidator
import v1.models.domain.Nino
import v1.models.request.AmendPensionCharges.{AmendPensionChargesRawData, AmendPensionChargesRequest, PensionCharges}
import v1.models.request.{AmendPensionCharges, _}

class AmendPensionChargesParser @Inject() (val validator: AmendPensionChargesValidator)
    extends RequestParser[AmendPensionChargesRawData, AmendPensionChargesRequest] {

  private def getBoolean(request: JsValue, key: String): JsValue = {
    val exists = (request \ "pensionContributions" \ key).isEmpty
    val value  = (request \ "pensionSavingsTaxCharges" \ key).asOpt[Boolean]
    exists match {
      case true if ((request \ "pensionSavingsTaxCharges" \ key).isEmpty == false) => {
        val jsonTransformer = (__ \ "pensionContributions" \ key).json.put(JsBoolean(value.get))

        request.transform(jsonTransformer) match {
          case JsSuccess(value, _) =>
            request.as[JsObject].deepMerge(value)
          case JsError(errors) =>
            throw new Exception()
        }
      }
      case _ => request
    }
  }

  def updateJson(value: JsValue): JsValue = {
    val setAllowanceReduced = getBoolean(value, "isAnnualAllowanceReduced")
    val setTaperedAllowance = getBoolean(setAllowanceReduced, "taperedAnnualAllowance")
    getBoolean(setTaperedAllowance, "moneyPurchasedAllowance")

  }

  override protected def requestFor(data: AmendPensionChargesRawData): AmendPensionChargesRequest = {

    val updatedJson = updateJson(data.body.json)
    AmendPensionCharges.AmendPensionChargesRequest(Nino(data.nino), TaxYear.fromMtd(data.taxYear), updatedJson.as[PensionCharges])
  }

}
