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

package v2.retrieve

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import shared.controllers.RequestContext
import shared.services.ServiceOutcome
import v2.retrieve.model.request.RetrievePensionChargesRequestData
import v2.retrieve.model.response.RetrievePensionChargesResponse

import scala.concurrent.{ExecutionContext, Future}

trait MockRetrievePensionsChargesService extends MockFactory {

  val mockRetrievePensionsChargesService: RetrievePensionChargesService = mock[RetrievePensionChargesService]

  object MockRetrievePensionsChargesService {

    def retrieve(
        retrievePensionChargesRequest: RetrievePensionChargesRequestData): CallHandler[Future[ServiceOutcome[RetrievePensionChargesResponse]]] = {
      (mockRetrievePensionsChargesService
        .retrievePensions(_: RetrievePensionChargesRequestData)(_: RequestContext, _: ExecutionContext))
        .expects(retrievePensionChargesRequest, *, *)
    }

  }

}
