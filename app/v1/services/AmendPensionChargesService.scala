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

package v1.services

import api.models.errors._
import cats.data.EitherT
import cats.implicits._
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v1.connectors.PensionChargesConnector
import v1.controllers.EndpointLogContext
import v1.models.request.AmendPensionCharges.AmendPensionChargesRequest
import v1.support.DownstreamResponseMappingSupport

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AmendPensionChargesService @Inject() (connector: PensionChargesConnector) extends DownstreamResponseMappingSupport with Logging {

  def amendPensions(request: AmendPensionChargesRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[AmendPensionChargesOutcome] = {

    val result = EitherT(connector.amendPensionCharges(request)).leftMap(mapDownstreamErrors(errorMap))

    result.value
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
