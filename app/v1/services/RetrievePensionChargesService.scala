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

import anyVersion.models.request.retrievePensionCharges.RetrievePensionChargesRequest
import api.controllers.RequestContext
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.BaseService
import cats.data.EitherT
import cats.implicits._
import config.{AppConfig, FeatureSwitches}
import v1.connectors.RetrievePensionChargesConnector
import v1.models.response.retrievePensionCharges.RetrievePensionChargesResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrievePensionChargesService @Inject() (connector: RetrievePensionChargesConnector, appConfig: AppConfig) extends BaseService {

  def retrievePensions(
      request: RetrievePensionChargesRequest)(implicit ctx: RequestContext, ec: ExecutionContext): Future[RetrievePensionChargesOutcome] = {

    EitherT(connector.retrievePensionCharges(request))
      .leftMap(mapDownstreamErrors(downstreamErrorMap))
      .flatMap(response => EitherT.fromEither[Future](cl102ResponseMap(response)))
      .value
  }

  def cl102ResponseMap(responseWrapper: ResponseWrapper[RetrievePensionChargesResponse])(implicit ctx: RequestContext): RetrievePensionChargesOutcome = {
    val response = responseWrapper.responseData

    val modifiedResponse = if (FeatureSwitches(appConfig.featureSwitches).isCL102Enabled) {
      response.addFieldsFromPensionContributionsToPensionSavingsTaxCharges
    } else {
      response
    }

    // This field is mandatory from a vendors perspective, so we have to throw an error if it's missing
    if (modifiedResponse.isIsAnnualAllowanceReducedMissing) {
      Left(ErrorWrapper(ctx.correlationId, InternalError))
    } else {
      val modifiedResponseWrapper = responseWrapper.copy(responseData = modifiedResponse.removeFieldsFromPensionContributions)
      Right(modifiedResponseWrapper)
    }
  }

  private def downstreamErrorMap: Map[String, MtdError] = {
    val errors = Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_TAX_YEAR"          -> TaxYearFormatError,
      "NO_DATA_FOUND"             -> NotFoundError,
      "INVALID_CORRELATIONID"     -> InternalError,
      "SERVER_ERROR"              -> InternalError,
      "SERVICE_UNAVAILABLE"       -> InternalError
    )
    val extraTysErrors = Map(
      "NOT_FOUND"              -> NotFoundError,
      "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError
    )

    errors ++ extraTysErrors
  }

}
