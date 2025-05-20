import Dependencies.*
import sbt.url
import sbtrelease.ReleaseStateTransformations.*

ThisBuild / organization := "uk.gov.nationalarchives"
ThisBuild / organizationName := "The National Archives"

scalaVersion := "3.4.0"
version := version.value

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/nationalarchives/da-metadata-schema"),
    "git@github.com:nationalarchives/da-metadata-schema.git"
  )
)

developers := List(
  Developer(
    id = "tna-da-bot",
    name = "TNA Digital Archiving",
    email = "s-GitHubDABot@nationalarchives.gov.uk",
    url = url("https://github.com/nationalarchives/da-metadata-schema")
  )
)

ThisBuild / description := "JSON Schema to describe The National Archives catalogue metadata"
ThisBuild / licenses := List("MIT" -> new java.net.URI("https://choosealicense.com/licenses/mit/").toURL)
ThisBuild / homepage := Some(url("https://github.com/nationalarchives/da-metadata-schema"))

useGpgPinentry := true
publishTo := sonatypePublishToBundle.value
publishMavenStyle := true

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  releaseStepTask(copySchema),
  releaseStepTask(copyValidationMessageProperties),
  releaseStepTask(copyGuidanceProperties),
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommand("publishSigned"),
  releaseStepCommand("sonatypeBundleRelease"),
  setNextVersion,
  commitNextVersion,
  pushChanges
)

resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

lazy val root = (project in file("."))
  .settings(
    name := "da-metadata-schema",
    libraryDependencies ++= Seq(
      commonsLang3,
      scalaTest % Test,
      jsonSchemaValidator,
      circeCore,
      circeGeneric,
      circeParser,
      ujson
    )
  )

lazy val copySchema = taskKey[Unit]("copySchema")
copySchema := {
  IO.copyDirectory(new File("metadata-schema"), new File(s"target/scala-${scalaVersion.value}/classes/metadata-schema"))
  IO.copyDirectory(new File("config-schema"), new File(s"target/scala-${scalaVersion.value}/classes/config-schema"))
}

lazy val copyValidationMessageProperties = taskKey[Unit]("copyValidationMessageProperties")
copyValidationMessageProperties := {
  IO.copyDirectory(new File("validation-messages"), new File(s"target/scala-${scalaVersion.value}/classes/validation-messages"))
}

lazy val copyGuidanceProperties = taskKey[Unit]("copyGuidanceProperties")
copyGuidanceProperties := {
  IO.copyDirectory(new File("guidance"), new File(s"target/scala-${scalaVersion.value}/classes/guidance"))
}
