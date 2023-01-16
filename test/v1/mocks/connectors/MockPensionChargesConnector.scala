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

package v1.mocks.connectors

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier
import v1.connectors.{DownstreamOutcome, PensionChargesConnector}
import v1.models.request.AmendPensionCharges.AmendPensionChargesRequest
import v1.models.request.DeletePensionCharges.DeletePensionChargesRequest
import v1.models.request.RetrievePensionCharges.RetrievePensionChargesRequest
import v1.models.response.retrieve.RetrievePensionChargesResponse

import scala.concurrent.{ExecutionContext, Future}

trait MockPensionChargesConnector extends MockFactory {

  val connector: PensionChargesConnector = mock[PensionChargesConnector]

  object MockPensionChargesConnector {

    def deletePensionCharges(deletePensionChargesRequest: DeletePensionChargesRequest): CallHandler[Future[DownstreamOutcome[Unit]]] = {
      (connector
        .deletePensionCharges(_: DeletePensionChargesRequest)(_: HeaderCarrier, _: ExecutionContext, _: String))
        .expects(deletePensionChargesRequest, *, *, *)
    }

    def amendPensionCharges(amendPensionChargesRequest: AmendPensionChargesRequest): CallHandler[Future[DownstreamOutcome[Unit]]] = {
      (connector
        .amendPensionCharges(_: AmendPensionChargesRequest)(_: HeaderCarrier, _: ExecutionContext, _: String))
        .expects(amendPensionChargesRequest, *, *, *)
    }

    def retrievePensions(request: RetrievePensionChargesRequest): CallHandler[Future[DownstreamOutcome[RetrievePensionChargesResponse]]] = {
      (connector
        .retrievePensionCharges(_: RetrievePensionChargesRequest)(_: HeaderCarrier, _: ExecutionContext, _: String))
        .expects(request, *, *, *)
    }

  }

}
