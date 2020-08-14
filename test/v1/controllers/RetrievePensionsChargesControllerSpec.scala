/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.controllers

import play.api.mvc.Result
import data.RetrievePensionChargesData.{fullJson, retrieveResponse}
import mocks.MockAppConfig
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.hateoas.MockHateoasFactory
import v1.mocks.requestParsers.MockRetrievePensionChargesParser
import v1.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService, MockRetrievePensionsChargesService}
import v1.models.outcomes.DesResponse
import v1.models.requestData.{DesTaxYear, RetrievePensionChargesRawData, RetrievePensionChargesRequest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrievePensionsChargesControllerSpec extends ControllerBaseSpec
  with MockEnrolmentsAuthService
  with MockMtdIdLookupService
  with MockRetrievePensionChargesParser
  with MockRetrievePensionsChargesService
  with MockHateoasFactory
  with MockAppConfig
  with MockAuditService
  {

    val correlationId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"
    val nino   = "AA123456A"
    val taxYear = "2020-21"

    val rawData = RetrievePensionChargesRawData(nino, taxYear)
    val requestData = RetrievePensionChargesRequest(Nino(nino),DesTaxYear(taxYear))

    trait Test {
      val hc = HeaderCarrier()

      val controller = new RetrievePensionChargesController(
        authService = mockEnrolmentsAuthService,
        lookupService = mockMtdIdLookupService,
        requestParser = mockRetrievePensionChargesParser,
        service = mockRetrievePensionsChargesService,
        hateoasFactory = mockHateoasFactory,
        auditService = mockAuditService,
        cc = cc
      )

      MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
      MockedEnrolmentsAuthService.authoriseUser()
    }

    "retrieve" should {
      "return a successful response with header X-CorrelationId and body" when {
        "the request received is valid" in new Test {

          MockRetrievePensionChargesParser
            .parseRequest(rawData)
            .returns(Right(requestData))

          MockRetrievePensionsChargesService
            .retrieve(requestData)
            .returns(Future.successful(Right(DesResponse(correlationId, retrieveResponse))))

          val result: Future[Result] = controller.retrieve(nino, taxYear)(fakeRequest)
          status(result) shouldBe OK
          contentAsJson(result) shouldBe fullJson
          header("X-CorrelationId", result) shouldBe Some(correlationId)
        }
      }
    }
}
