/*
 * Copyright 2025 HM Revenue & Customs
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

package v3.highIncomeChildBenefitCharge.retrieve

import play.api.Configuration
import play.api.mvc.Result
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.domain.TaxYear
import shared.models.errors.{ErrorWrapper, NinoFormatError, NotFoundError}
import shared.models.outcomes.ResponseWrapper
import v3.highIncomeChildBenefitCharge.retrieve.RetrieveHighIncomeChildBenefitFixtures.{responseJson, responseModel}
import v3.highIncomeChildBenefitCharge.retrieve.model.RetrieveHighIncomeChildBenefitChargeRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveHighIncomeChildBenefitChargeControllerSpec extends ControllerBaseSpec with ControllerTestRunner {

  private val taxYear: String = "2025-26"

  private val requestData: RetrieveHighIncomeChildBenefitChargeRequest =
    RetrieveHighIncomeChildBenefitChargeRequest(parsedNino, TaxYear.fromMtd(taxYear))

  "RetrieveHighIncomeChildBenefitChargeController" should {
    "return a successful response with status 200 (OK)" when {
      "given a valid request" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveHighIncomeChildBenefitService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseModel))))

        runOkTest(
          expectedStatus = OK,
          maybeExpectedResponseBody = Some(responseJson)
        )
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveHighIncomeChildBenefitService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, NotFoundError))))

        runErrorTest(NotFoundError)
      }
    }

  }

  trait Test
      extends ControllerTest
      with MockRetrieveHighIncomeChildBenefitChargeService
      with MockRetrieveHighIncomeChildBenefitChargeValidatorFactory {

    val controller: RetrieveHighIncomeChildBenefitChargeController = new RetrieveHighIncomeChildBenefitChargeController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveHighIncomeChildBenefitChargeValidatorFactory,
      service = mockRetrieveHighIncomeChildBenefitChargeService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.retrieve(validNino, taxYear)(fakeRequest)
  }

}
