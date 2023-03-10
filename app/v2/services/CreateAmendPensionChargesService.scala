/*
 * Copyright 2023 HM Revenue & Customs
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

package v2.services

import api.controllers.RequestContext
import api.models.errors._
import api.services.BaseService
import cats.implicits._
import v2.connectors.CreateAmendPensionChargesConnector
import v2.models.request.createAmendPensionCharges.CreateAmendPensionChargesRequest

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAmendPensionChargesService @Inject()(connector: CreateAmendPensionChargesConnector) extends BaseService {

  def createAmendPensions(request: CreateAmendPensionChargesRequest)(implicit ctx: RequestContext, ec: ExecutionContext): Future[CreateAmendPensionChargesOutcome] = {
    connector
      .createAmendPensionCharges(request)
      .map(_.leftMap(mapDownstreamErrors(errorMap)))
  }

  private val errorMap: Map[String, MtdError] = {
    val errors = Map(
      "INVALID_TAXABLE_ENTITY_ID"    -> NinoFormatError,
      "INVALID_TAX_YEAR"             -> TaxYearFormatError,
      "INVALID_PAYLOAD"              -> RuleIncorrectOrEmptyBodyError,
      "INVALID_CORRELATIONID"        -> InternalError,
      "REDUCTION_TYPE_NOT_SPECIFIED" -> InternalError,
      "REDUCTION_NOT_SPECIFIED"      -> InternalError,
      "SERVER_ERROR"                 -> InternalError,
      "SERVICE_UNAVAILABLE"          -> InternalError
    )
    val extraTysErrors = Map(
      "MISSING_ANNUAL_ALLOWANCE_REDUCTION" -> InternalError,
      "MISSING_TYPE_OF_REDUCTION"          -> InternalError,
      "TAX_YEAR_NOT_SUPPORTED"             -> RuleTaxYearNotSupportedError
    )

    errors ++ extraTysErrors
  }

}
