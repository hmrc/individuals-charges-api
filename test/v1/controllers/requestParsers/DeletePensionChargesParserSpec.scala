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

package v1.controllers.requestParsers

import org.scalamock.handlers.CallHandler1
import org.scalamock.scalatest.MockFactory
import support.UnitSpec
import uk.gov.hmrc.domain.Nino
import v1.controllers.requestParsers.validators.Validator
import v1.models.errors._
import v1.models.requestData.{DeletePensionChargesRawData, DeletePensionChargesRequest, DesTaxYear}

class DeletePensionChargesParserSpec extends UnitSpec{
  val nino = "AA123456B"
  val taxYear = "2019-20"

  val inputData = DeletePensionChargesRawData(nino, taxYear)

  //TODO REPLACE WITH ACTUAL MOCK VALIDATOR
  class MockDeletePensionChargesValidator extends MockFactory {

    val mockValidator: TempValidator = mock[TempValidator]

    object MockValidator {
      def validate(data: DeletePensionChargesRawData): CallHandler1[DeletePensionChargesRawData, List[MtdError]] = {
        (mockValidator.validate(_: DeletePensionChargesRawData)).expects(data)
      }
    }
  }

  trait Test extends MockDeletePensionChargesValidator {
    lazy val parser = new DeletePensionChargesParser(mockValidator)
  }

  "parse" should {

    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockValidator.validate(inputData).returns(Nil)

        parser.parseRequest(inputData) shouldBe Right(DeletePensionChargesRequest(Nino(nino), DesTaxYear(taxYear)))
      }
    }

    "return an ErrorWrapper" when {

      "a single validation error occurs" in new Test {
        MockValidator.validate(inputData).returns(List(NinoFormatError))

        parser.parseRequest(inputData) shouldBe Left(ErrorWrapper(None, Seq(NinoFormatError)))
      }

      "multiple validation errors occur" in new Test {
        MockValidator.validate(inputData).returns(List(NinoFormatError, LossIdFormatError))

        parser.parseRequest(inputData) shouldBe Left(ErrorWrapper(None, Seq(BadRequestError, NinoFormatError, LossIdFormatError)))
      }
    }
  }
}
