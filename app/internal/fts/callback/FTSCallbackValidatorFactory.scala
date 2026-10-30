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

package internal.fts.callback

import api.controllers.validators.Validator
import internal.fts.callback.model.FTSCallbackRequestData
import play.api.libs.json.JsValue

import javax.inject.{Inject, Singleton}

@Singleton
class FTSCallbackValidatorFactory @Inject() {

  def validator(body: JsValue): Validator[FTSCallbackRequestData] = {
    new FTSCallbackValidator(body)
  }

}
