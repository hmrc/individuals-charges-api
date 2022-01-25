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
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"   %% "bootstrap-backend-play-28" % "5.19.0",
    "org.typelevel" %% "cats-core"                 % "2.7.0",
    "com.chuusai"   %% "shapeless"                 % "2.4.0-M1",
    compilerPlugin("com.github.ghik" % "silencer-plugin" % "1.7.0" cross CrossVersion.full),
    "com.github.ghik"    % "silencer-lib"   % "1.7.0" % Provided cross CrossVersion.full,
    "org.json4s"         %% "json4s-native" % "3.6.12",
    "org.json4s"         %% "json4s-ext"    % "3.6.12",
    "com.neovisionaries" % "nv-i18n"        % "1.29"
  )

  def test(scope: String = "test, it"): Seq[sbt.ModuleID] = Seq(
    "org.scalatest"          %% "scalatest"            % "3.2.10"            % scope,
    "com.vladsch.flexmark"   % "flexmark-all"          % "0.62.2"            % scope,
    "org.scalacheck"         %% "scalacheck"           % "1.15.4"            % scope,
    "org.scalamock"          %% "scalamock"            % "5.2.0"             % scope,
    "org.pegdown"            % "pegdown"               % "1.6.0"             % scope,
    "com.typesafe.play"      %% "play-test"            % PlayVersion.current % scope,
    "org.scalatestplus.play" %% "scalatestplus-play"   % "5.1.0"             % scope,
    "com.github.tomakehurst" % "wiremock-jre8"         % "2.27.2"            % scope,
    "com.github.fge"         % "json-schema-validator" % "2.2.6"             % scope
  )
}
