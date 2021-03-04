/*
 * Copyright 2021 HM Revenue & Customs
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
import v1.models.errors.ValueFormatError

class NumberValidationSpec extends UnitSpec {

  val number = Some(BigDecimal(123.45))
  val path = "path"

  "Number validation" when {
    "invalid number is supplied" must {
      "return errors" in {
        NumberValidation.validateOptional(Some(BigDecimal(-9.00)), path) shouldBe List(
          ValueFormatError.copy(paths = Some(Seq(path)))
        )
      }
    }
    "valid number is supplied" must {
      "return no errors" in {
        NumberValidation.validateOptional(number, "path") shouldBe NoValidationErrors
      }
    }
    "none is supplied" must {
      "return no errors" in {
        NumberValidation.validateOptional(None, "path") shouldBe NoValidationErrors
      }
    }
  }
}
