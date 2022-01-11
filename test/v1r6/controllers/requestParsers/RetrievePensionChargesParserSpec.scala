/*
 * Copyright 2022 HM Revenue & Customs
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

package v1r6.controllers.requestParsers

import support.UnitSpec
import v1r6.mocks.validators.MockRetrievePensionChargesValidator
import v1r6.models.domain.Nino
import v1r6.models.errors.{BadRequestError, ErrorWrapper, LossIdFormatError, NinoFormatError}
import v1r6.models.request.DesTaxYear
import v1r6.models.request.RetrievePensionCharges.{RetrievePensionChargesRawData, RetrievePensionChargesRequest}

class RetrievePensionChargesParserSpec extends UnitSpec{
  val nino = "AA123456B"
  val taxYear = "2019-20"
  implicit val correlationId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  val inputData = RetrievePensionChargesRawData(nino, taxYear)

  trait Test extends MockRetrievePensionChargesValidator {
    lazy val parser = new RetrievePensionChargesParser(mockValidator)
  }

  "parse" should {

    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockValidator.validate(inputData).returns(Nil)

        parser.parseRequest(inputData) shouldBe Right(RetrievePensionChargesRequest(Nino(nino), DesTaxYear(taxYear)))
      }
    }

    "return an ErrorWrapper" when {

      "a single validation error occurs" in new Test {
        MockValidator.validate(inputData).returns(List(NinoFormatError))

        parser.parseRequest(inputData) shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "multiple validation errors occur" in new Test {
        MockValidator.validate(inputData).returns(List(NinoFormatError, LossIdFormatError))

        parser.parseRequest(inputData) shouldBe Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, LossIdFormatError))))
      }
    }
  }
}