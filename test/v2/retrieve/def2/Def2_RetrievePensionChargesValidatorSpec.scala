/*
 * Copyright 2024 HM Revenue & Customs
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

package v2.retrieve.def2

import play.api.Configuration
import shared.config.MockSharedAppConfig
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors.{ErrorWrapper, NinoFormatError, RuleTaxYearRangeInvalidError, TaxYearFormatError}
import shared.utils.UnitSpec
import v2.retrieve.RetrievePensionChargesValidatorFactory
import v2.retrieve.def2.model.request.Def2_RetrievePensionChargesRequestData
import v2.retrieve.model.request.RetrievePensionChargesRequestData

class Def2_RetrievePensionChargesValidatorSpec extends UnitSpec with MockSharedAppConfig {
  private implicit val correlationId: String = "1234"

  private val validNino    = "AA123456A"
  private val validTaxYear = "2024-25"

  private val parsedNino    = Nino(validNino)
  private val parsedTaxYear = TaxYear.fromMtd(validTaxYear)

  private val validatorFactory = new RetrievePensionChargesValidatorFactory(mockSharedAppConfig)

  private def validator(nino: String, taxYear: String) = validatorFactory.validator(nino, taxYear)

  private def setupMocks = MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
    "removeLifetimePension.enabled" -> true
  )

  class Test {}

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in new Test {
        setupMocks
        val result: Either[ErrorWrapper, RetrievePensionChargesRequestData] =
          validator(validNino, validTaxYear).validateAndWrapResult()

        result shouldBe Right(Def2_RetrievePensionChargesRequestData(parsedNino, parsedTaxYear))
      }
    }

    "should return a single error" when {
      "an invalid nino is supplied" in new Test {
        setupMocks
        val result: Either[ErrorWrapper, RetrievePensionChargesRequestData] =
          validator("invalidNino", validTaxYear).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "an incorrectly formatted taxYear is supplied" in new Test {
        setupMocks
        val result: Either[ErrorWrapper, RetrievePensionChargesRequestData] =
          validator(validNino, "202122").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }

      "an invalid tax year range is supplied" in new Test {
        setupMocks
        val result: Either[ErrorWrapper, RetrievePensionChargesRequestData] =
          validator(validNino, "2020-22").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }
    }
  }

}
