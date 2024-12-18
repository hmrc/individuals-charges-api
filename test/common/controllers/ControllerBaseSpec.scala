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

package common.controllers

import common.models.audit.{AuditError, AuditEvent, AuditResponse, GenericAuditDetail}
import common.errors.{BadRequestError, ErrorWrapper, MtdError}
import common.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import cats.implicits.catsSyntaxValidatedId
import config.Deprecation.NotDeprecated
import config.RealAppConfig
import mocks.{MockIndividualsChargesConfig, MockIdGenerator}
import play.api.http.{HeaderNames, MimeTypes, Status}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsEmpty, ControllerComponents, Result}
import play.api.test.Helpers.stubControllerComponents
import play.api.test.{FakeRequest, ResultExtractors}
import routing.{Version, Version2}
import support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

abstract class ControllerBaseSpec
    extends UnitSpec
    with Status
    with MimeTypes
    with HeaderNames
    with ResultExtractors
    with MockAuditService
    with MockIndividualsChargesConfig {

  lazy val cc: ControllerComponents = stubControllerComponents()

  implicit val apiVersion: Version = Version2

  implicit lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest().withHeaders(HeaderNames.ACCEPT -> s"application/vnd.hmrc.${apiVersion.name}+json")

  def fakePostRequest[T](body: T): FakeRequest[T] = fakeRequest.withBody(body)

  def fakePutRequest[T](body: T): FakeRequest[T] = fakeRequest.withBody(body)

}

trait ControllerTestRunner extends MockEnrolmentsAuthService with MockMtdIdLookupService with MockIdGenerator with RealAppConfig {
  _: ControllerBaseSpec =>
  protected val nino: String  = ControllerTestRunner.validNino
  protected val correlationId = "X-123"

  trait ControllerTest {
    protected val hc: HeaderCarrier = HeaderCarrier()

    protected val controller: AuthorisedController

    protected def callController(): Future[Result]

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.generateCorrelationId.returns(correlationId)

    MockedIndividualsChargesConfig
      .deprecationFor(apiVersion)
      .returns(NotDeprecated.valid)
      .anyNumberOfTimes()

    protected def runOkTest(expectedStatus: Int, maybeExpectedResponseBody: Option[JsValue] = None): Unit = {
      val result: Future[Result] = callController()

      status(result) shouldBe expectedStatus
      header("X-CorrelationId", result) shouldBe Some(correlationId)

      maybeExpectedResponseBody match {
        case Some(jsBody) => contentAsJson(result) shouldBe jsBody
        case None         => contentType(result) shouldBe empty
      }

      checkEmaConfig()
    }

    protected def runErrorTest(expectedError: MtdError): Unit = {
      val result: Future[Result] = callController()

      status(result) shouldBe expectedError.httpStatus
      header("X-CorrelationId", result) shouldBe Some(correlationId)

      contentAsJson(result) shouldBe expectedError.asJson
    }

    protected def runMultipleErrorsTest(expectedErrors: Seq[MtdError]): Unit = {
      val expectedError = ErrorWrapper(correlationId, BadRequestError, Some(expectedErrors))

      val result: Future[Result] = callController()

      status(result) shouldBe BAD_REQUEST
      header("X-CorrelationId", result) shouldBe Some(correlationId)
      contentAsJson(result) shouldBe Json.toJson(expectedError)
    }

    private def checkEmaConfig(): Unit = {
      val endpoints: Map[String, Boolean] = emaEndpoints

      val endpointSupportingAgentsAllowed: Boolean =
        endpoints
          .getOrElse(
            controller.endpointName,
            fail(s"Controller endpoint name \"${controller.endpointName}\" not found in application.conf.")
          )

      realAppConfig.endpointAllowsSupportingAgents(controller.endpointName) shouldBe endpointSupportingAgentsAllowed
    }

  }

  trait AuditEventChecking {
    _: ControllerTest =>

    protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail]

    protected def runOkTestWithAudit(expectedStatus: Int,
                                     maybeExpectedResponseBody: Option[JsValue] = None,
                                     maybeAuditRequestBody: Option[JsValue] = None,
                                     maybeAuditResponseBody: Option[JsValue] = None): Unit = {
      runOkTest(expectedStatus, maybeExpectedResponseBody)
      checkAuditOkEvent(expectedStatus, maybeAuditRequestBody, maybeAuditResponseBody)
    }

    protected def checkAuditOkEvent(expectedStatus: Int, maybeRequestBody: Option[JsValue], maybeAuditResponseBody: Option[JsValue]): Unit = {
      val auditResponse: AuditResponse = AuditResponse(expectedStatus, None, maybeAuditResponseBody)
      MockedAuditService.verifyAuditEvent(event(auditResponse, maybeRequestBody)).once()
    }

    protected def runErrorTestWithAudit(expectedError: MtdError, maybeAuditRequestBody: Option[JsValue] = None): Unit = {
      runErrorTest(expectedError)
      checkAuditErrorEvent(expectedError, maybeAuditRequestBody)
    }

    protected def checkAuditErrorEvent(expectedError: MtdError, maybeRequestBody: Option[JsValue]): Unit = {
      val auditResponse: AuditResponse = AuditResponse(expectedError.httpStatus, Some(List(AuditError(expectedError.code))), None)
      MockedAuditService.verifyAuditEvent(event(auditResponse, maybeRequestBody)).once()
    }

    protected def runMultipleErrorsTestWithAudit(expectedErrors: Seq[MtdError], maybeAuditRequestBody: Option[JsValue] = None): Unit = {
      runMultipleErrorsTest(expectedErrors)
      checkAuditMultipleErrorsEvent(expectedErrors, maybeAuditRequestBody)
    }

    protected def checkAuditMultipleErrorsEvent(errors: Seq[MtdError], maybeRequestBody: Option[JsValue]): Unit = {
      val auditErrors = errors.map(err => AuditError(err.code))

      val auditResponse: AuditResponse =
        AuditResponse(
          httpStatus = BAD_REQUEST,
          errors = Some(auditErrors),
          body = None
        )
      MockedAuditService.verifyAuditEvent(event(auditResponse, maybeRequestBody)).once()
    }

  }

}

object ControllerTestRunner {
  val validNino: String = "AA123456A"
}
