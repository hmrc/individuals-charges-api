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

package config

import cats.data.Validated
import cats.implicits.catsSyntaxValidatedId
import com.typesafe.config.Config
import config.Deprecation.{Deprecated, NotDeprecated}
import play.api.{ConfigLoader, Configuration}
import routing.Version
import uk.gov.hmrc.auth.core.ConfidenceLevel
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.LocalDateTime
import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}
import java.time.temporal.ChronoField
import javax.inject.{Inject, Singleton}

trait IndividualsChargesConfig {

  def appName: String

  // MTD ID Lookup Config
  def mtdIdBaseUrl: String

  // DES Config
  def desBaseUrl: String
  def desEnv: String
  def desToken: String
  def desEnvironmentHeaders: Option[Seq[String]]

  lazy val desDownstreamConfig: DownstreamConfig =
    DownstreamConfig(baseUrl = desBaseUrl, env = desEnv, token = desToken, environmentHeaders = desEnvironmentHeaders)

  // IFS Config
  def ifsBaseUrl: String
  def ifsEnv: String
  def ifsToken: String
  def ifsEnvironmentHeaders: Option[Seq[String]]

  lazy val ifsDownstreamConfig: DownstreamConfig =
    DownstreamConfig(baseUrl = ifsBaseUrl, env = ifsEnv, token = ifsToken, environmentHeaders = ifsEnvironmentHeaders)

  // API Config
  def apiGatewayContext: String
  def apiStatus(version: Version): String
  def featureSwitchConfig: Configuration
  def endpointsEnabled(version: String): Boolean
  def endpointsEnabled(version: Version): Boolean
  def apiVersionReleasedInProduction(version: String): Boolean
  def endpointReleasedInProduction(version: String, name: String): Boolean
  def safeEndpointsEnabled(version: String): Boolean

  def confidenceLevelConfig: ConfidenceLevelConfig
  def minTaxYearPensionCharge: String

  def endpointAllowsSupportingAgents(endpointName: String): Boolean
  def deprecationFor(version: Version): Validated[String, Deprecation]
  def apiDocumentationUrl: String

}

@Singleton
class IndividualsChargesConfigImpl @Inject()(config: ServicesConfig, protected[config] val configuration: Configuration) extends IndividualsChargesConfig {

  // MTD ID Lookup COnfig
  val mtdIdBaseUrl: String = config.baseUrl("mtd-id-lookup")

  // DES Config
  val desBaseUrl: String = config.baseUrl("des")
  val desEnv: String     = config.getString("microservice.services.des.env")
  val desToken: String   = config.getString("microservice.services.des.token")

  val desEnvironmentHeaders: Option[Seq[String]] =
    configuration.getOptional[Seq[String]]("microservice.services.des.environmentHeaders")

  // IFS Config
  val ifsBaseUrl: String = config.baseUrl("ifs")
  val ifsEnv: String     = config.getString("microservice.services.ifs.env")
  val ifsToken: String   = config.getString("microservice.services.ifs.token")

  val ifsEnvironmentHeaders: Option[Seq[String]] =
    configuration.getOptional[Seq[String]]("microservice.services.ifs.environmentHeaders")

  // API Config
  val apiGatewayContext: String                   = config.getString("api.gateway.context")
  def apiStatus(version: Version): String         = config.getString(s"api.$version.status")
  def featureSwitchConfig: Configuration          = configuration.getOptional[Configuration](s"feature-switch").getOrElse(Configuration.empty)
  def endpointsEnabled(version: String): Boolean  = config.getBoolean(s"api.$version.endpoints.enabled")
  def endpointsEnabled(version: Version): Boolean = config.getBoolean(s"api.${version.name}.endpoints.enabled")

  def apiVersionReleasedInProduction(version: String): Boolean = config.getBoolean(s"api.$version.endpoints.api-released-in-production")

  def endpointReleasedInProduction(version: String, name: String): Boolean = {
    val versionReleasedInProd = apiVersionReleasedInProduction(version)
    val path                  = s"api.$version.endpoints.released-in-production.$name"

    val conf = configuration.underlying
    if (versionReleasedInProd && conf.hasPath(path)) config.getBoolean(path) else versionReleasedInProd
  }

  val confidenceLevelConfig: ConfidenceLevelConfig =
    configuration.get[ConfidenceLevelConfig](s"api.confidence-level-check")

  def minTaxYearPensionCharge: String = configuration.getOptional[String]("minTaxYearPensionCharge").getOrElse("2022")

  def safeEndpointsEnabled(version: String): Boolean =
    configuration
      .getOptional[Boolean](s"api.$version.endpoints.enabled")
      .getOrElse(false)

  def endpointAllowsSupportingAgents(endpointName: String): Boolean =
    supportingAgentEndpoints.getOrElse(endpointName, false)

  private val supportingAgentEndpoints: Map[String, Boolean] =
    configuration
      .getOptional[Map[String, Boolean]]("api.supporting-agent-endpoints")
      .getOrElse(Map.empty)

  def appName: String = config.getString("appName")

  def apiDocumentationUrl: String =
    configuration
      .get[Option[String]]("api.documentation-url")
      .getOrElse(s"https://developer.service.hmrc.gov.uk/api-documentation/docs/api/service/$appName")

  private val DATE_FORMATTER = new DateTimeFormatterBuilder()
    .append(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    .parseDefaulting(ChronoField.HOUR_OF_DAY, 23)
    .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 59)
    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 59)
    .toFormatter()

  def deprecationFor(version: Version): Validated[String, Deprecation] = {
    val isApiDeprecated: Boolean = apiStatus(version) == "DEPRECATED"

    val deprecatedOn: Option[LocalDateTime] =
      configuration
        .getOptional[String](s"api.$version.deprecatedOn")
        .map(value => LocalDateTime.parse(value, DATE_FORMATTER))

    val sunsetDate: Option[LocalDateTime] =
      configuration
        .getOptional[String](s"api.$version.sunsetDate")
        .map(value => LocalDateTime.parse(value, DATE_FORMATTER))

    val isSunsetEnabled: Boolean =
      configuration.getOptional[Boolean](s"api.$version.sunsetEnabled").getOrElse(true)

    if (isApiDeprecated) {
      (deprecatedOn, sunsetDate, isSunsetEnabled) match {
        case (Some(dO), Some(sD), true) =>
          if (sD.isAfter(dO)) { Deprecated(dO, Some(sD)).valid }
          else { s"sunsetDate must be later than deprecatedOn date for a deprecated version $version".invalid }
        case (Some(dO), None, true) => Deprecated(dO, Some(dO.plusMonths(6).plusDays(1))).valid
        case (Some(dO), _, false)   => Deprecated(dO, None).valid
        case _                      => s"deprecatedOn date is required for a deprecated version $version".invalid
      }

    } else { NotDeprecated.valid }

  }

}

trait FixedConfig {
  // Minimum tax year for MTD
  val minimumTaxYearBFLoss    = 2019
  val minimumTaxYearLossClaim = 2020
}

case class ConfidenceLevelConfig(confidenceLevel: ConfidenceLevel, definitionEnabled: Boolean, authValidationEnabled: Boolean)

object ConfidenceLevelConfig {

  implicit val configLoader: ConfigLoader[ConfidenceLevelConfig] = (rootConfig: Config, path: String) => {
    val config = rootConfig.getConfig(path)
    ConfidenceLevelConfig(
      confidenceLevel = ConfidenceLevel.fromInt(config.getInt("confidence-level")).get,
      definitionEnabled = config.getBoolean("definition.enabled"),
      authValidationEnabled = config.getBoolean("auth-validation.enabled")
    )
  }

}
