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

import common.errors.SourceFormatError
import shared.models.domain.MtdSourceEnum.latest
import shared.models.domain.{MtdSourceEnum, Nino, TaxYear}
import shared.models.errors.*
import shared.utils.UnitSpec
import v3.winterFuelPayment.retrieve.model.request.RetrieveWinterFuelPaymentRequestData

class RetrieveWinterFuelPaymentValidatorSpec extends UnitSpec {

  private implicit val correlationId: String = "1234"

  private val validNino: String           = "AA123456A"
  private val validTaxYear: String        = "2026-27"
  private val validSource: Option[String] = Some("hmrc-held")

  private val parsedNino: Nino            = Nino(validNino)
  private val parsedTaxYear: TaxYear      = TaxYear.fromMtd(validTaxYear)
  private val parsedSource: MtdSourceEnum = MtdSourceEnum.`hmrc-held`

  def validator(nino: String, taxYear: String, maybeSource: Option[String]): RetrieveWinterFuelPaymentValidator =
    new RetrieveWinterFuelPaymentValidator(nino, taxYear, maybeSource)

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied with source" in {
        val result: Either[ErrorWrapper, RetrieveWinterFuelPaymentRequestData] =
          validator(validNino, validTaxYear, validSource).validateAndWrapResult()

        result shouldBe Right(RetrieveWinterFuelPaymentRequestData(parsedNino, parsedTaxYear, parsedSource))
      }

      "a valid request is supplied without source" in {
        val result: Either[ErrorWrapper, RetrieveWinterFuelPaymentRequestData] =
          validator(validNino, validTaxYear, None).validateAndWrapResult()

        result shouldBe Right(RetrieveWinterFuelPaymentRequestData(parsedNino, parsedTaxYear, latest))
      }
    }

    "return NinoFormatError error" when {
      "an invalid nino is supplied" in {
        val result: Either[ErrorWrapper, RetrieveWinterFuelPaymentRequestData] =
          validator("A12344A", validTaxYear, validSource).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }
    }

    "return TaxYearFormatError error" when {
      "an invalid tax year is supplied" in {
        val result: Either[ErrorWrapper, RetrieveWinterFuelPaymentRequestData] = validator(validNino, "20267", validSource).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }
    }

    "return RuleTaxYearRangeInvalidError error" when {
      "an invalid tax year range is supplied" in {
        val result: Either[ErrorWrapper, RetrieveWinterFuelPaymentRequestData] = validator(validNino, "2026-28", validSource).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }
    }

    "return RuleTaxYearNotSupportedError error" when {
      "an unsupported tax year is supplied" in {
        val result: Either[ErrorWrapper, RetrieveWinterFuelPaymentRequestData] = validator(validNino, "2018-19", validSource).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }
    }

    "return SourceFormatError error" when {
      "an invalid source is supplied" in {
        val result: Either[ErrorWrapper, RetrieveWinterFuelPaymentRequestData] =
          validator(validNino, validTaxYear, Some("invalid-source")).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, SourceFormatError))
      }
    }

    "return multiple errors" when {
      "request supplied has multiple errors" in {
        val result: Either[ErrorWrapper, RetrieveWinterFuelPaymentRequestData] =
          validator(validNino, "20267", Some("invalid-source")).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BadRequestError, Some(List(SourceFormatError, TaxYearFormatError))))
      }
    }
  }

}
