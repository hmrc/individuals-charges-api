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

package v3.winterFuelPayment.retrieve

import shared.connectors.DownstreamOutcome
import shared.models.domain.*
import shared.models.errors.*
import shared.models.outcomes.ResponseWrapper
import shared.services.{ServiceOutcome, ServiceSpec}
import v3.winterFuelPayment.retrieve.RetrieveWinterFuelPaymentFixtures.responseModel
import v3.winterFuelPayment.retrieve.model.request.RetrieveWinterFuelPaymentRequestData
import v3.winterFuelPayment.retrieve.model.response.RetrieveWinterFuelPaymentResponse

import scala.concurrent.Future

class RetrieveWinterFuelPaymentServiceSpec extends ServiceSpec {
  private val nino: Nino            = Nino("AA123456A")
  private val taxYear: TaxYear      = TaxYear.fromMtd("2026-27")
  private val source: MtdSourceEnum = MtdSourceEnum.`hmrc-held`

  implicit override val correlationId: String = "X-123"

  "RetrieveWinterFuelPaymentService" when {
    "retrieve" should {
      "return correct result for a success" in new Test {
        val outcome: ServiceOutcome[RetrieveWinterFuelPaymentResponse] =
          Right(ResponseWrapper(correlationId, responseModel))

        val connectorOutcome: DownstreamOutcome[RetrieveWinterFuelPaymentResponse] =
          Right(ResponseWrapper(correlationId, responseModel))

        MockRetrieveWinterFuelPaymentConnector.retrieve(request).returns(Future.successful(connectorOutcome))

        val result: ServiceOutcome[RetrieveWinterFuelPaymentResponse] = await(service.retrieve(request))

        result shouldBe outcome
      }

      "map errors according to spec" when {
        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {

            MockRetrieveWinterFuelPaymentConnector
              .retrieve(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            val result: ServiceOutcome[RetrieveWinterFuelPaymentResponse] = await(service.retrieve(request))

            result shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errors = List(
          ("1117", TaxYearFormatError),
          ("1215", NinoFormatError),
          ("1216", InternalError),
          ("5010", NotFoundError),
          ("5000", InternalError)
        )

        errors.foreach(serviceError.tupled)
      }
    }

  }

  trait Test extends MockRetrieveWinterFuelPaymentConnector {

    val request: RetrieveWinterFuelPaymentRequestData = RetrieveWinterFuelPaymentRequestData(
      nino = nino,
      taxYear = taxYear,
      source = source
    )

    val service: RetrieveWinterFuelPaymentService = new RetrieveWinterFuelPaymentService(
      connector = mockRetrieveWinterFuelPaymentConnector
    )

  }

}
