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

package v3.highIncomeChildBenefitCharge.delete

import shared.controllers.RequestContext
import shared.models.errors._
import shared.services.{BaseService, ServiceOutcome}
import cats.implicits.toBifunctorOps
import common.errors.RuleOutsideAmendmentWindow
import shared.models.errors.MtdError
import v3.highIncomeChildBenefitCharge.delete.model.request.DeleteHighIncomeChildBenefitChargeRequestData

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeleteHighIncomeChildBenefitChargeService @Inject()(connector: DeleteHighIncomeChildBenefitChargeConnector) extends BaseService {

  def deleteHighIncomeChildBenefit(
      request: DeleteHighIncomeChildBenefitChargeRequestData)(implicit ctx: RequestContext, ec: ExecutionContext): Future[ServiceOutcome[Unit]] = {
    connector
      .deleteHighIncomeChildBenefit(request)
      .map(_.leftMap(mapDownstreamErrors(downstreamErrorMap)))
  }



  private def downstreamErrorMap: Map[String, MtdError] =
    Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_TAX_YEAR"          -> TaxYearFormatError,
      "INVALID_CORRELATIONID"     -> InternalError,
      "NOT_FOUND"             -> NotFoundError,
      "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError,
      "OUTSIDE_AMENDMENT_WINDOW"        -> RuleOutsideAmendmentWindow,
      "SERVER_ERROR"              -> InternalError,
      "SERVICE_UNAVAILABLE"       -> InternalError,
    )
}
