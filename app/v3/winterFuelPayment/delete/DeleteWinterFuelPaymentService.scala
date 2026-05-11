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

package v3.winterFuelPayment.delete

import shared.controllers.RequestContext
import shared.models.errors.*
import shared.services.{BaseService, ServiceOutcome}
import cats.implicits.toBifunctorOps
import common.errors.RuleOutsideAmendmentWindowError
import shared.models.errors.MtdError
import v3.winterFuelPayment.delete.model.request.DeleteWinterFuelPaymentRequestData

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeleteWinterFuelPaymentService @Inject() (connector: DeleteWinterFuelPaymentConnector) extends BaseService {

  def delete(request: DeleteWinterFuelPaymentRequestData)(implicit ctx: RequestContext, ec: ExecutionContext): Future[ServiceOutcome[Unit]] = {
    connector
      .delete(request)
      .map(_.leftMap(mapDownstreamErrors(downstreamErrorMap)))
  }

  private def downstreamErrorMap: Map[String, MtdError] =
    Map(
      "1117" -> TaxYearFormatError,
      "1215" -> NinoFormatError,
      "1216" -> InternalError,
      "5010" -> NotFoundError,
      "4200" -> RuleOutsideAmendmentWindowError,
      "5000" -> InternalError
    )

}
