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

package v1.controllers.requestParsers

import api.models.domain.TaxYear
import api.models.errors.{BadRequestError, ErrorWrapper, NinoFormatError, TaxYearFormatError}
import play.api.mvc.AnyContentAsJson
import support.UnitSpec
import v1.data.AmendPensionChargesData
import v1.mocks.validators.MockAmendPensionChargesValidator
import api.models.domain.Nino
import v1.models.request.AmendPensionCharges.{AmendPensionChargesRawData, AmendPensionChargesRequest}

class AmendPensionChargesParserSpec extends UnitSpec {
  val nino                           = "AA123456B"
  val taxYear                        = "2019-20"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  val inputData: AmendPensionChargesRawData =
    AmendPensionChargesRawData(nino, taxYear, AnyContentAsJson(AmendPensionChargesData.fullJson))

  val inputDataUpdated: AmendPensionChargesRawData =
    AmendPensionChargesRawData(nino, taxYear, AnyContentAsJson(AmendPensionChargesData.fullValidJsonUpdated))

  trait Test extends MockAmendPensionChargesValidator {
    lazy val parser = new AmendPensionChargesParser(mockValidator)
  }

  "parse" should {

    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockValidator.validate(inputData).returns(Nil)

        parser.parseRequest(inputData) shouldBe
          Right(AmendPensionChargesRequest(Nino(nino), TaxYear.fromMtd(taxYear), AmendPensionChargesData.pensionCharges))
      }
      "valid updated request data is supplied" in new Test {
        MockValidator.validate(inputDataUpdated).returns(Nil)

        parser.parseRequest(inputDataUpdated) shouldBe
          Right(AmendPensionChargesRequest(Nino(nino), TaxYear.fromMtd(taxYear), AmendPensionChargesData.pensionCharges))
      }
    }

    "return an ErrorWrapper" when {

      "a single validation error occurs" in new Test {
        MockValidator.validate(inputData).returns(List(NinoFormatError))

        parser.parseRequest(inputData) shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "multiple validation errors occur" in new Test {
        MockValidator.validate(inputData).returns(List(NinoFormatError, TaxYearFormatError))

        parser.parseRequest(inputData) shouldBe Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }
    }
  }

}
