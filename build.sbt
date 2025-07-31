import sbt.{Compile, *}
import sbt.Keys.baseDirectory
import uk.gov.hmrc.DefaultBuildSettings
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

ThisBuild / scalaVersion := "3.5.2"
ThisBuild / majorVersion := 1
ThisBuild / scalacOptions ++= Seq(
  "-Werror",
  "-Wconf:msg=Flag.*repeatedly:s"
)

val appName = "individuals-charges-api"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) // Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    retrieveManaged                 := true,
    update / evictionWarningOptions := EvictionWarningOptions.default.withWarnScalaVersionEviction(warnScalaVersionEviction = false),
    scalafmtOnCompile               := true,
    scalacOptions ++= List(
      "-Wconf:src=routes/.*:s",
      "-feature"
    )
  )
  .settings(
    Compile / unmanagedResourceDirectories += baseDirectory.value / "resources",
    Compile / unmanagedClasspath += baseDirectory.value / "resources"
  )
  .settings(CodeCoverageSettings.settings *)
  .settings(PlayKeys.playDefaultPort := 9765)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings())
  .settings(
    Test / fork                       := true,
    Test / javaOptions += "-Dlogger.resource=logback-test.xml",
  )
  .settings(libraryDependencies ++= AppDependencies.itDependencies)

