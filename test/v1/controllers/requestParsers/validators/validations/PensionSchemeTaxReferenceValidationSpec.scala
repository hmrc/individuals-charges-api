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

package v1.controllers.requestParsers.validators.validations

import support.UnitSpec
import v1.models.errors.PensionSchemeTaxRefFormatError

class PensionSchemeTaxReferenceValidationSpec extends UnitSpec {

  "SF74RefValidation" when {
    "validateOptional" must {
      "validate correctly for some valid sf74Ref" in {
        PensionSchemeTaxReferenceValidation.validate(
          pensionSchemeTaxRef = "00123456RA",
          path = "/path"
        ) shouldBe NoValidationErrors
      }

      "validate correctly for some invalid sf74Ref" in {
        PensionSchemeTaxReferenceValidation.validate(
          pensionSchemeTaxRef = "This pensionSchemeTaxRef string is 91 characters long ---------------------------------------------- 91",
          path = "/path"
        ) shouldBe List(PensionSchemeTaxRefFormatError.copy(paths = Some(Seq("/path"))))
      }
    }
  }

}
