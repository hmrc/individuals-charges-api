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

package v1r6.controllers.requestParsers

import javax.inject.Inject
import v1r6.controllers.requestParsers.validators.RetrievePensionChargesValidator
import v1r6.models.domain.Nino
import v1r6.models.request.{DesTaxYear, RetrievePensionCharges}
import v1r6.models.request.RetrievePensionCharges.{RetrievePensionChargesRawData, RetrievePensionChargesRequest}

class RetrievePensionChargesParser @Inject()(val validator: RetrievePensionChargesValidator) extends RequestParser[RetrievePensionChargesRawData,
  RetrievePensionChargesRequest] {

  override protected def requestFor(data: RetrievePensionChargesRawData): RetrievePensionChargesRequest =
    RetrievePensionCharges.RetrievePensionChargesRequest(Nino(data.nino), DesTaxYear(data.taxYear))
}
