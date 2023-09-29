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

package v2.controllers.validators

import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import mocks.MockAppConfig
import support.UnitSpec
import v2.models.request.retrievePensionCharges.RetrievePensionChargesRequestData

class RetrievePensionChargesValidatorFactorySpec extends UnitSpec with MockAppConfig {
  private implicit val correlationId: String = "1234"

  private val validNino                      = "AA123456A"
  private val validTaxYear                   = "2021-22"

  private val parsedNino    = Nino(validNino)
  private val parsedTaxYear = TaxYear.fromMtd(validTaxYear)

  private val validatorFactory = new RetrievePensionChargesValidatorFactory(mockAppConfig)

  private def validator(nino: String, taxYear: String) = validatorFactory.validator(nino, taxYear)

  MockAppConfig.minTaxYearPensionCharge.returns("2022")

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {
        val result: Either[ErrorWrapper, RetrievePensionChargesRequestData] =
          validator(validNino, validTaxYear).validateAndWrapResult()

        result shouldBe Right(RetrievePensionChargesRequestData(parsedNino, parsedTaxYear))
      }
    }

    // invalid nino
    "should return a single error" when {
      "an invalid nino is supplied" in {
        val result: Either[ErrorWrapper, RetrievePensionChargesRequestData] =
          validator("invalidNino", validTaxYear).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      // incorrect tax year format
      "an incorrectly formatted taxYear is supplied" in {
        val result: Either[ErrorWrapper, RetrievePensionChargesRequestData] =
          validator(validNino, "202122").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }

      // invalid tax year range
      "an invalid tax year range is supplied" in {
        val result: Either[ErrorWrapper, RetrievePensionChargesRequestData] =
          validator(validNino, "2020-22").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }

      // invalid tax year - before minimum tax year value
      "an invalid tax year, before the minimum, is supplied" in {
        val result: Either[ErrorWrapper, RetrievePensionChargesRequestData] =
          validator(validNino, "2020-21").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }
    }

    // multiple errors
    "return multiple errors" when {
      "request supplied has multiple errors" in {
        val result: Either[ErrorWrapper, RetrievePensionChargesRequestData] =
          validator("invalidNino", "invalidTaxYear").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BadRequestError, Some(List(NinoFormatError, TaxYearFormatError))))
      }
    }
  }

}

