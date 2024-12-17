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

package mocks

import cats.data.Validated
import config.{ConfidenceLevelConfig, Deprecation, IndividualsChargesConfig}
import org.scalamock.handlers.{CallHandler, CallHandler0}
import org.scalamock.scalatest.MockFactory
import play.api.Configuration
import routing.Version
import shared.config.MockSharedAppConfig

trait MockIndividualsChargesConfig extends MockFactory with MockSharedAppConfig {

  implicit val mockAppConfig: IndividualsChargesConfig = mock[IndividualsChargesConfig]

  object MockedIndividualsChargesConfig {

    def desBaseUrl: CallHandler[String] = (() => mockAppConfig.desBaseUrl).expects()

    def desToken: CallHandler[String] = (() => mockAppConfig.desToken).expects()

    def desEnvironment: CallHandler[String] = (() => mockAppConfig.desEnv).expects()

    def desEnvironmentHeaders: CallHandler[Option[Seq[String]]] = (() => mockAppConfig.desEnvironmentHeaders).expects()

    // IFS config
    def ifsBaseUrl: CallHandler[String]                         = (() => mockAppConfig.ifsBaseUrl).expects()
    def ifsToken: CallHandler[String]                           = (() => mockAppConfig.ifsToken).expects()
    def ifsEnvironment: CallHandler[String]                     = (() => mockAppConfig.ifsEnv).expects()
    def ifsEnvironmentHeaders: CallHandler[Option[Seq[String]]] = (() => mockAppConfig.ifsEnvironmentHeaders).expects()

    def mtdIdBaseUrl: CallHandler[String] = (() => mockAppConfig.mtdIdBaseUrl).expects()

    def featureSwitchConfig: CallHandler[Configuration] = (() => mockAppConfig.featureSwitchConfig).expects()

    def apiGatewayContext: CallHandler[String] = (() => mockAppConfig.apiGatewayContext).expects()

    def apiStatus(status: Version): CallHandler[String]          = (mockAppConfig.apiStatus: Version => String).expects(status)
    def endpointsEnabled(version: String): CallHandler[Boolean]  = (mockAppConfig.endpointsEnabled(_: String)).expects(version)
    def endpointsEnabled(version: Version): CallHandler[Boolean] = (mockAppConfig.endpointsEnabled(_: Version)).expects(version)

    def apiVersionReleasedInProduction(version: String): CallHandler[Boolean] =
      (mockAppConfig.apiVersionReleasedInProduction: String => Boolean).expects(version)

    def endpointReleasedInProduction(version: String, key: String): CallHandler[Boolean] =
      (mockAppConfig.endpointReleasedInProduction: (String, String) => Boolean).expects(version, key)

    def minTaxYearPensionCharge: CallHandler[String] = (() => mockAppConfig.minTaxYearPensionCharge).expects()

    def confidenceLevelConfig: CallHandler0[ConfidenceLevelConfig] =
      (() => mockAppConfig.confidenceLevelConfig).expects()

    def confidenceLevelCheckEnabled: CallHandler[ConfidenceLevelConfig] =
      (() => mockAppConfig.confidenceLevelConfig).expects()

    def endpointAllowsSupportingAgents(endpointName: String): CallHandler[Boolean] =
      (mockAppConfig.endpointAllowsSupportingAgents(_: String)).expects(endpointName)

    def deprecationFor(version: Version): CallHandler[Validated[String, Deprecation]] = (mockAppConfig.deprecationFor(_: Version)).expects(version)

    def apiDocumentationUrl(): CallHandler[String] = (() => mockAppConfig.apiDocumentationUrl: String).expects()

  }

}
