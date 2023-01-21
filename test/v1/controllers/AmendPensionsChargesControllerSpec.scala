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

import api.models.errors.{ErrorWrapper, NinoFormatError, RuleIncorrectOrEmptyBodyError}
import app.controllers.{ControllerBaseSpec, ControllerTestRunner}
import mocks.MockAppConfig
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsJson, Result}
import v1.data.AmendPensionChargesData._
import v1.mocks.MockIdGenerator
import v1.mocks.hateoas.MockHateoasFactory
import v1.mocks.requestParsers.MockAmendPensionChargesParser
import v1.mocks.services._
import v1.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import v1.models.domain.Nino
import v1.models.hateoas.Method.{DELETE, GET, PUT}
import v1.models.hateoas.{HateoasWrapper, Link}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.AmendPensionCharges.{AmendPensionChargesRawData, AmendPensionChargesRequest}
import v1.models.request.TaxYear
import v1.models.response.amend.AmendPensionChargesHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendPensionsChargesControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAmendPensionChargesParser
    with MockAmendPensionsChargesService
    with MockAppConfig
    with MockAuditService
    with MockHateoasFactory
    with MockIdGenerator {

  private val taxYear     = "2021-22"
  private val rawData     = AmendPensionChargesRawData(nino, taxYear, AnyContentAsJson(fullJson))
  private val requestData = AmendPensionChargesRequest(Nino(nino), TaxYear.fromMtd(taxYear), pensionCharges)

  private val testHateoasLinks = Seq(
    Link(href = s"/individuals/charges/pensions/$nino/$taxYear", method = GET, rel = "self"),
    Link(href = s"/individuals/charges/pensions/$nino/$taxYear", method = PUT, rel = "create-and-amend-charges-pensions"),
    Link(href = s"/individuals/charges/pensions/$nino/$taxYear", method = DELETE, rel = "delete-charges-pensions")
  )

  val hateoasResponse: JsValue = Json.parse(
    s"""
       |{
       |   "links":[
       |      {
       |         "href":"/individuals/charges/pensions/$nino/$taxYear",
       |         "method":"GET",
       |         "rel":"self"
       |      },
       |      {
       |         "href":"/individuals/charges/pensions/$nino/$taxYear",
       |         "method":"PUT",
       |         "rel":"create-and-amend-charges-pensions"
       |      },
       |      {
       |         "href":"/individuals/charges/pensions/$nino/$taxYear",
       |         "method":"DELETE",
       |         "rel":"delete-charges-pensions"
       |      }
       |   ]
       |}
    """.stripMargin
  )

  class Test extends ControllerTest {

    val controller = new AmendPensionChargesController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockAmendPensionChargesParser,
      service = mockAmendPensionsChargesService,
      auditService = mockAuditService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    override protected def callController(): Future[Result] = controller.amend(nino, taxYear)(fakePostRequest(fullJson))
  }

  def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
    AuditEvent(
      auditType = "CreateAmendPensionsCharges",
      transactionName = "create-amend-pensions-charges",
      detail = GenericAuditDetail(
        userType = "Individual",
        agentReferenceNumber = None,
        params = Map("nino" -> nino, "taxYear" -> taxYear),
        request = requestBody,
        `X-CorrelationId` = correlationId,
        response = auditResponse
      )
    )

  "amend" should {
    "return a successful response with header X-CorrelationId and body" when {
      "the request received is valid" in new Test {

        MockAmendPensionChargesParser
          .parseRequest(rawData)
          .returns(Right(requestData))

        MockAmendPensionsChargesService
          .amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), AmendPensionChargesHateoasData(nino, taxYear))
          .returns(HateoasWrapper((), testHateoasLinks))

        val result: Future[Result] = controller.amend(nino, taxYear)(fakePutRequest(fullJson))
        status(result) shouldBe OK
        contentAsJson(result) shouldBe hateoasResponse
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(OK, None, Some(hateoasResponse))
        MockedAuditService.verifyAuditEvent(event(auditResponse, Some(fullJson))).once
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockAmendPensionChargesParser
          .parseRequest(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        MockAmendPensionChargesParser
          .parseRequest(rawData)
          .returns(Right(requestData))

        MockAmendPensionsChargesService
          .amend(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))))

        runErrorTest(RuleIncorrectOrEmptyBodyError)
      }
    }
  }

}
