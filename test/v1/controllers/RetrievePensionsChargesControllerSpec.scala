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

package v1.controllers

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.hateoas.{HateoasWrapper, MockHateoasFactory}
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.{Nino, TaxYear}
import api.models.errors.{ErrorWrapper, NinoFormatError, TaxYearFormatError}
import api.models.outcomes.ResponseWrapper
import api.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import mocks.{MockAppConfig, MockIdGenerator}
import play.api.libs.json.{JsObject, JsValue}
import play.api.mvc.Result
import v1.data.RetrievePensionChargesData.{fullJsonCl102FieldsInTaxCharges, retrieveResponseCl102FieldsInTaxCharges}
import v1.mocks.requestParsers.MockRetrievePensionChargesParser
import v1.mocks.services._
import v1.models.request.retrievePensionCharges.{RetrievePensionChargesRawData, RetrievePensionChargesRequestData}
import v1.models.response.retrievePensionCharges.RetrievePensionChargesHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrievePensionsChargesControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrievePensionChargesParser
    with MockRetrievePensionsChargesService
    with MockHateoasFactory
    with MockAppConfig
    with MockAuditService
    with MockIdGenerator {

  private val taxYear = "2021-22"

  private val rawData     = RetrievePensionChargesRawData(nino, taxYear)
  private val requestData = RetrievePensionChargesRequestData(Nino(nino), TaxYear.fromMtd(taxYear))

  class Test extends ControllerTest with AuditEventChecking {

    val controller = new RetrievePensionChargesController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockRetrievePensionChargesParser,
      service = mockRetrievePensionsChargesService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    override protected def callController(): Future[Result] = controller.retrieve(nino, taxYear)(fakeGetRequest)

    override protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "RetrievePensionsCharges",
        transactionName = "retrieve-pensions-charges",
        detail = GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          params = Map("nino" -> nino, "taxYear" -> taxYear),
          request = None,
          `X-CorrelationId` = correlationId,
          response = auditResponse
        )
      )

  }

  "retrieve" should {
    "return a successful response with header X-CorrelationId and body" when {
      "the request received is valid" in new Test {

        MockRetrievePensionChargesParser
          .parseRequest(rawData)
          .returns(Right(requestData))

        MockRetrievePensionsChargesService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, retrieveResponseCl102FieldsInTaxCharges))))

        MockHateoasFactory
          .wrap(retrieveResponseCl102FieldsInTaxCharges, RetrievePensionChargesHateoasData(nino, taxYear))
          .returns(HateoasWrapper(retrieveResponseCl102FieldsInTaxCharges, links = hateoaslinks))

        runOkTest(OK, Some(fullJsonCl102FieldsInTaxCharges.as[JsObject] ++ hateoaslinksJson))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockRetrievePensionChargesParser
          .parseRequest(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        MockRetrievePensionChargesParser
          .parseRequest(rawData)
          .returns(Right(requestData))

        MockRetrievePensionsChargesService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, TaxYearFormatError))))

        runErrorTest(TaxYearFormatError)
      }
    }

  }

}
