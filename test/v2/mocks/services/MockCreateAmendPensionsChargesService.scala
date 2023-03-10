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

package v2.mocks.services

import api.controllers.RequestContext
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import v2.models.request.createAmendPensionCharges.CreateAmendPensionChargesRequest
import v2.services.{CreateAmendPensionChargesOutcome, CreateAmendPensionChargesService}

import scala.concurrent.{ExecutionContext, Future}

trait MockCreateAmendPensionsChargesService extends MockFactory {

  val mockCreateAmendPensionsChargesService: CreateAmendPensionChargesService = mock[CreateAmendPensionChargesService]

  object MockAmendPensionsChargesService {

    def amend(amendPensionChargesRequest: CreateAmendPensionChargesRequest): CallHandler[Future[CreateAmendPensionChargesOutcome]] = {
      (mockCreateAmendPensionsChargesService
        .createAmendPensions(_: CreateAmendPensionChargesRequest)(_: RequestContext, _: ExecutionContext))
        .expects(amendPensionChargesRequest, *, *)
    }

  }

}
