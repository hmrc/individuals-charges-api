import sbt._
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings}
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

val appName = "individuals-charges-api"

lazy val ItTest = config("it") extend Test

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test(),
    retrieveManaged := true,
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    scalaVersion := "2.12.12"
  )
  .settings(
    unmanagedResourceDirectories in Compile += baseDirectory.value / "resources"
  )
  .settings(Test/fork:=true)
  .settings(majorVersion := 0)
  .settings(publishingSettings: _*)
  .settings(CodeCoverageSettings.settings: _*)
  .settings(defaultSettings(): _*)
  .configs(ItTest)
  .settings(inConfig(ItTest)(Defaults.itSettings): _*)
  .settings(
    fork in ItTest := true,
    unmanagedSourceDirectories in ItTest := Seq((baseDirectory in ItTest).value / "it"),
    unmanagedClasspath in ItTest += baseDirectory.value / "resources",
    unmanagedClasspath in Runtime += baseDirectory.value / "resources",
    javaOptions in ItTest += "-Dlogger.resource=logback-test.xml",
    parallelExecution in ItTest := false,
    addTestReportOption(ItTest, "int-test-reports")
  )
  .settings(
    resolvers += Resolver.jcenterRepo
  )
  .settings(PlayKeys.playDefaultPort := 9765)
  .settings(SilencerSettings())
