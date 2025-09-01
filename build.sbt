import Dependencies._
import sbt.url
import sbtrelease.ReleaseStateTransformations._

ThisBuild / organization := "uk.gov.nationalarchives"
ThisBuild / organizationName := "The National Archives"

scalaVersion := "2.13.16"
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
crossTarget := target.value / s"scala-${scalaVersion.value}"

useGpgPinentry := true
publishTo := {
  val centralSnapshots = "https://central.sonatype.com/repository/maven-snapshots/"
  if (isSnapshot.value) Some("central-snapshots" at centralSnapshots)
  else localStaging.value
}
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
  releaseStepCommand("sonaRelease"),
  setNextVersion,
  commitNextVersion,
  pushChanges
)

lazy val root = (project in file("."))
  .settings(
    name := "da-metadata-schema",
    libraryDependencies ++= Seq(
      commonsLang3,
      scalaTest % Test,
      jsonSchemaValidator,
      circeCore,
      circeGeneric,
      circeGenericExtras,
      circeParser,
      ujson
    ),
    Test / resourceGenerators += Def.task {
      val base = baseDirectory.value
      val out = (Test / resourceManaged).value
      val dirs = Seq("metadata-schema", "config-schema", "validation-messages", "guidance")
      val copied = dirs.flatMap { d =>
        val src = base / d
        val dest = out / d
        IO.copyDirectory(src, dest)
        (dest ** "*").get
      }
      copied
    }.taskValue
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
