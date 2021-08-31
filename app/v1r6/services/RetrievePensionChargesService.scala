/*
 * Copyright 2021 HM Revenue & Customs
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

package v1r6.services

import cats.data.EitherT
import cats.implicits._
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v1r6.connectors.PensionChargesConnector
import v1r6.controllers.EndpointLogContext
import v1r6.models.errors._
import v1r6.models.request.RetrievePensionCharges.RetrievePensionChargesRequest
import v1r6.support.DesResponseMappingSupport

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrievePensionChargesService @Inject()(connector: PensionChargesConnector) extends DesResponseMappingSupport with Logging {

  def retrievePensions(request: RetrievePensionChargesRequest)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext,
    logContext: EndpointLogContext,
    correlationId: String): Future[RetrievePensionChargesOutcome] = {

    val result = for {
      desResponseWrapper <- EitherT(connector.retrievePensionCharges(request)).leftMap(mapDesErrors(desErrorMap))
    } yield desResponseWrapper

    result.value
  }

  private def desErrorMap : Map[String, MtdError] = Map(
    "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
    "INVALID_TAX_YEAR"          -> TaxYearFormatError,
    "NO_DATA_FOUND"             -> NotFoundError,
    "INVALID_CORRELATIONID"     -> DownstreamError,
    "SERVER_ERROR"              -> DownstreamError,
    "SERVICE_UNAVAILABLE"       -> DownstreamError
  )
}
