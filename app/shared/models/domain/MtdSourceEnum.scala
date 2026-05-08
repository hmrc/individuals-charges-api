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

package shared.models.domain

import play.api.libs.json.Writes
import shared.utils.enums.Enums

enum MtdSourceEnum(val toDownstreamViewString: String) {
  case `hmrc-held` extends MtdSourceEnum("HMRC-HELD")
  case user        extends MtdSourceEnum("CUSTOMER")
  case latest      extends MtdSourceEnum("LATEST")
}

object MtdSourceEnum {
  given Writes[MtdSourceEnum]                        = Enums.writes[MtdSourceEnum]
  val parser: PartialFunction[String, MtdSourceEnum] = Enums.parser[MtdSourceEnum](values)
}
