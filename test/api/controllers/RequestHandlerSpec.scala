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

package api.controllers

import api.controllers.validators.Validator
import api.models.audit.{AuditError, AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.auth.UserDetails
import api.models.errors.{ErrorWrapper, InternalError, MtdError, NinoFormatError}
import api.models.outcomes.ResponseWrapper
import api.services.{MockAuditService, ServiceOutcome}
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.implicits.catsSyntaxValidatedId
import config.Deprecation.{Deprecated, NotDeprecated}
import config.{IndividualsChargesConfig, Deprecation}
import mocks.{MockIndividualsChargesConfig, MockIdGenerator}
import org.scalamock.handlers.CallHandler
import play.api.http.{HeaderNames, Status}
import play.api.libs.json.{JsString, Json, OWrites}
import play.api.mvc.{AnyContent, AnyContentAsEmpty}
import play.api.test.{FakeRequest, ResultExtractors}
import routing.{Version, Version2}
import support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class RequestHandlerSpec
    extends UnitSpec
    with MockAuditService
    with MockIdGenerator
    with Status
    with HeaderNames
    with ResultExtractors
    with MockIndividualsChargesConfig {

  private val successResponseJson = Json.obj("result" -> "SUCCESS!")
  private val successCode         = ACCEPTED

  private val generatedCorrelationId = "generatedCorrelationId"
  private val serviceCorrelationId   = "serviceCorrelationId"
  private val userDetails            = UserDetails("mtdId", "Individual", Some("agentReferenceNumber"))
  private val mockService            = mock[DummyService]

  private def service =
    (mockService.service(_: Input.type)(_: RequestContext, _: ExecutionContext)).expects(Input, *, *)

  case object Input
  case object Output { implicit val writes: OWrites[Output.type] = _ => successResponseJson }

  MockIdGenerator.generateCorrelationId.returns(generatedCorrelationId).anyNumberOfTimes()

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "SomeController", endpointName = "someEndpoint")

  implicit val hc: HeaderCarrier   = HeaderCarrier()
  implicit val ctx: RequestContext = RequestContext.from(mockIdGenerator, endpointLogContext)

  private val versionHeader = HeaderNames.ACCEPT -> "application/vnd.hmrc.3.0+json"

  implicit val userRequest: UserRequest[AnyContent] = {
    val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withHeaders(versionHeader)
    UserRequest[AnyContent](userDetails, fakeRequest)
  }

  trait DummyService {
    def service(input: Input.type)(implicit ctx: RequestContext, ec: ExecutionContext): Future[ServiceOutcome[Output.type]]
  }

  def mockDeprecation(deprecationStatus: Deprecation): CallHandler[Validated[String, Deprecation]] =
    MockedIndividualsChargesConfig
      .deprecationFor(Version(userRequest))
      .returns(deprecationStatus.valid)
      .anyNumberOfTimes()

  private val successValidatorForRequest = new Validator[Input.type] {
    def validate: Validated[Seq[MtdError], Input.type] = Valid(Input)
  }

  private val singleErrorValidatorForRequest = new Validator[Input.type] {
    def validate: Validated[Seq[MtdError], Input.type] = Invalid(List(NinoFormatError))
  }

  implicit val appConfig: IndividualsChargesConfig = mockAppConfig
  implicit val apiVersion: Version  = Version(userRequest)

  "RequestHandler" when {
    "given a request" must {
      "return the correct response" in {
        val requestHandler = RequestHandler
          .withValidator(successValidatorForRequest)
          .withService(mockService.service)
          .withPlainJsonResult(successCode)

        mockDeprecation(NotDeprecated)
        service returns Future.successful(Right(ResponseWrapper(serviceCorrelationId, Output)))

        val result = requestHandler.handleRequest()

        contentAsJson(result) shouldBe successResponseJson
        header("X-CorrelationId", result) shouldBe Some(serviceCorrelationId)
        status(result) shouldBe successCode
      }

      "return no content if required" in {
        val requestHandler = RequestHandler
          .withValidator(successValidatorForRequest)
          .withService(mockService.service)
          .withNoContentResult()

        mockDeprecation(NotDeprecated)
        service returns Future.successful(Right(ResponseWrapper(serviceCorrelationId, Output)))

        val result = requestHandler.handleRequest()

        contentAsString(result) shouldBe ""
        header("X-CorrelationId", result) shouldBe Some(serviceCorrelationId)
        status(result) shouldBe NO_CONTENT
      }
    }

    "a request fails with validation errors" must {
      "return the errors" in {
        val requestHandler = RequestHandler
          .withValidator(singleErrorValidatorForRequest)
          .withService(mockService.service)
          .withPlainJsonResult(successCode)

        mockDeprecation(NotDeprecated)
        val result = requestHandler.handleRequest()

        contentAsJson(result) shouldBe NinoFormatError.asJson
        header("X-CorrelationId", result) shouldBe Some(generatedCorrelationId)
        status(result) shouldBe NinoFormatError.httpStatus
      }
    }

    "a request fails with service errors" must {
      "return the errors" in {
        val requestHandler = RequestHandler
          .withValidator(successValidatorForRequest)
          .withService(mockService.service)
          .withPlainJsonResult(successCode)

        mockDeprecation(NotDeprecated)
        service returns Future.successful(Left(ErrorWrapper(serviceCorrelationId, NinoFormatError)))

        val result = requestHandler.handleRequest()

        contentAsJson(result) shouldBe NinoFormatError.asJson
        header("X-CorrelationId", result) shouldBe Some(serviceCorrelationId)
        status(result) shouldBe NinoFormatError.httpStatus
      }
    }

    "auditing is configured" when {
      val params    = Map("param" -> "value")
      val auditType = "type"
      val txName    = "txName"

      val requestBody = Some(JsString("REQUEST BODY"))

      def auditHandler(includeResponse: Boolean = false): AuditHandler = AuditHandler(
        mockAuditService,
        auditType = auditType,
        transactionName = txName,
        apiVersion = Version2,
        params = params,
        requestBody = requestBody,
        includeResponse = includeResponse
      )

      val basicRequestHandler = RequestHandler
        .withValidator(successValidatorForRequest)
        .withService(mockService.service)
        .withPlainJsonResult(successCode)

      def verifyAudit(correlationId: String, auditResponse: AuditResponse): CallHandler[Future[AuditResult]] =
        MockedAuditService.verifyAuditEvent(
          AuditEvent(
            auditType = auditType,
            transactionName = txName,
            GenericAuditDetail(
              userDetails,
              params = params,
              apiVersion = Version2.name,
              requestBody = requestBody,
              `X-CorrelationId` = correlationId,
              auditResponse = auditResponse)
          ))

      "a request is successful" when {
        "no response is to be audited" must {
          "audit without the response" in {
            val requestHandler = basicRequestHandler.withAuditing(auditHandler())

            mockDeprecation(NotDeprecated)
            service returns Future.successful(Right(ResponseWrapper(serviceCorrelationId, Output)))

            val result = requestHandler.handleRequest()

            contentAsJson(result) shouldBe successResponseJson
            header("X-CorrelationId", result) shouldBe Some(serviceCorrelationId)
            status(result) shouldBe successCode

            verifyAudit(serviceCorrelationId, AuditResponse(successCode, Right(None)))
          }
        }

        "the response is to be audited" must {
          "audit with the response" in {
            val requestHandler = basicRequestHandler.withAuditing(auditHandler(includeResponse = true))

            mockDeprecation(NotDeprecated)
            service returns Future.successful(Right(ResponseWrapper(serviceCorrelationId, Output)))

            val result = requestHandler.handleRequest()

            contentAsJson(result) shouldBe successResponseJson
            header("X-CorrelationId", result) shouldBe Some(serviceCorrelationId)
            status(result) shouldBe successCode

            verifyAudit(serviceCorrelationId, AuditResponse(successCode, Right(Some(successResponseJson))))
          }
        }
      }

      "a request fails with validation errors" must {
        "return the errors" in {
          val requestHandler = RequestHandler
            .withValidator(singleErrorValidatorForRequest)
            .withService(mockService.service)
            .withPlainJsonResult(successCode)

          mockDeprecation(NotDeprecated)

          val result = requestHandler.handleRequest()

          contentAsJson(result) shouldBe NinoFormatError.asJson
          header("X-CorrelationId", result) shouldBe Some(generatedCorrelationId)
          status(result) shouldBe NinoFormatError.httpStatus
        }
      }

      "a request fails with service errors" must {
        "audit the failure" in {
          val requestHandler = basicRequestHandler.withAuditing(auditHandler())

          mockDeprecation(NotDeprecated)
          service returns Future.successful(Left(ErrorWrapper(serviceCorrelationId, NinoFormatError)))

          val result = requestHandler.handleRequest()

          contentAsJson(result) shouldBe NinoFormatError.asJson
          header("X-CorrelationId", result) shouldBe Some(serviceCorrelationId)
          status(result) shouldBe NinoFormatError.httpStatus

          verifyAudit(serviceCorrelationId, AuditResponse(NinoFormatError.httpStatus, Left(List(AuditError(NinoFormatError.code)))))
        }
      }

      "a request is made to a deprecated version" must {
        "return the correct response" when {
          "deprecatedOn and sunsetDate exists" in {

            val requestHandler = basicRequestHandler.withAuditing(auditHandler())

            service returns Future.successful(Right(ResponseWrapper(serviceCorrelationId, Output)))

            mockDeprecation(
              Deprecated(
                deprecatedOn = LocalDateTime.of(2023, 1, 17, 12, 0),
                sunsetDate = Some(LocalDateTime.of(2024, 1, 17, 12, 0))
              )
            )

            MockedIndividualsChargesConfig.apiDocumentationUrl().returns("http://someUrl").anyNumberOfTimes()

            val result = requestHandler.handleRequest()

            contentAsJson(result) shouldBe successResponseJson
            header("X-CorrelationId", result) shouldBe Some(serviceCorrelationId)
            header("Deprecation", result) shouldBe Some("Tue, 17 Jan 2023 12:00:00 GMT")
            header("Sunset", result) shouldBe Some("Wed, 17 Jan 2024 12:00:00 GMT")
            header("Link", result) shouldBe Some("http://someUrl")

            status(result) shouldBe successCode
          }

          "only deprecatedOn exists" in {
            val requestHandler = basicRequestHandler.withAuditing(auditHandler())

            service returns Future.successful(Right(ResponseWrapper(serviceCorrelationId, Output)))

            mockDeprecation(
              Deprecated(
                deprecatedOn = LocalDateTime.of(2023, 1, 17, 12, 0),
                None
              )
            )
            MockedIndividualsChargesConfig.apiDocumentationUrl().returns("http://someUrl").anyNumberOfTimes()

            val result = requestHandler.handleRequest()

            contentAsJson(result) shouldBe successResponseJson
            header("X-CorrelationId", result) shouldBe Some(serviceCorrelationId)
            header("Deprecation", result) shouldBe Some("Tue, 17 Jan 2023 12:00:00 GMT")
            header("Sunset", result) shouldBe None
            header("Link", result) shouldBe Some("http://someUrl")
            status(result) shouldBe successCode
          }
        }
      }

    }
  }

  "given an error handler that doesn't handle an error" should {
    "return an InternalServerError" in {
      service returns Future.successful(Left(ErrorWrapper(serviceCorrelationId, NinoFormatError)))
      mockDeprecation(NotDeprecated)
      val errorHandler = ErrorHandling {
        case _: ErrorWrapper if false =>
          throw new Exception("Should not have been matched")
      }
      val requestHandler = RequestHandler
        .withValidator(successValidatorForRequest)
        .withService(mockService.service)
        .withErrorHandling(errorHandler)

      val result = requestHandler.handleRequest()
      status(result) shouldBe InternalError.httpStatus
      contentAsJson(result) shouldBe InternalError.asJson
    }
  }

  "withErrorHandling()" should {
    "return a new RequestHandlerBuilder with the expected error handling" in {
      class CustomErrorHandling extends ErrorHandling(null)

      val requestHandler = RequestHandler
        .withValidator(successValidatorForRequest)
        .withService(mockService.service)
        .withErrorHandling(new CustomErrorHandling)

      val result = requestHandler
      result.errorHandling shouldBe a[CustomErrorHandling]
    }
  }

}
