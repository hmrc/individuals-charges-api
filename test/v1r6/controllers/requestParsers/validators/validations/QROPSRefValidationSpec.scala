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

package v1r6.controllers.requestParsers.validators.validations

import support.UnitSpec
import v1r6.models.errors.QOPSRefFormatError

class QROPSRefValidationSpec extends UnitSpec {

  "QOPSRefValidation" when {
    "validate correctly for some valid qopsRef" in {
      QROPSRefValidation.validate("Q123456","path") shouldBe NoValidationErrors
      QROPSRefValidation.validate("Q143533","path") shouldBe NoValidationErrors
      QROPSRefValidation.validate("Q100000","path") shouldBe NoValidationErrors
    }

    "validate correctly for some invalid qopsRef" in {
      QROPSRefValidation.validate(
        "This qopsRef string is 91 characters long ---------------------------------------------- 91",
        "/overseasPensionContributions/0/qualifyingRecognisedOverseasPensionScheme"
      ) shouldBe List(QOPSRefFormatError.copy(paths = Some(Seq("/overseasPensionContributions/0/qualifyingRecognisedOverseasPensionScheme"))))
    }
  }
}
