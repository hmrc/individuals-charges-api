/*
 * PCopyright 2020 HM Revenue & Customs
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

import play.core.PlayVersion
import play.sbt.PlayImport.*
import sbt.*

object AppDependencies {
  val bootstrapPlayVersion = "9.13.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"                  %% "bootstrap-backend-play-30" % bootstrapPlayVersion,
    "org.typelevel"                %% "cats-core"                 % "2.13.0",
    "com.chuusai"                  %% "shapeless"                 % "2.4.0-M1",
    "com.neovisionaries"            % "nv-i18n"                   % "1.29",
    "com.fasterxml.jackson.module" %% "jackson-module-scala"      % "2.19.1",
    "com.github.jknack"             % "handlebars"                % "4.4.0"
  )

  def test(scope: String = "test, it"): Seq[sbt.ModuleID] = Seq(
    "org.scalatestplus"      %% "scalacheck-1-15"        % "3.2.11.0"           % scope,
    "org.scalacheck"         %% "scalacheck"             % "1.18.1"             % scope,
    "org.scalamock"          %% "scalamock"              % "7.3.2"              % scope,
    "uk.gov.hmrc"            %% "bootstrap-test-play-30" % bootstrapPlayVersion % scope,
    "com.github.fge"          % "json-schema-validator"  % "2.2.6"              % scope,
    "io.swagger.parser.v3"    % "swagger-parser-v3"      % "2.1.29"             % scope
  )

}
