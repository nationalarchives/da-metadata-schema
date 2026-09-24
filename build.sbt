import Dependencies._
import sbt.uri
import sbtrelease.ReleaseStateTransformations._

ThisBuild / organization := "uk.gov.nationalarchives"
ThisBuild / organizationName := "The National Archives"

ThisBuild / scalaVersion := "2.13.16"

ThisBuild / scmInfo := Some(
  ScmInfo(
    uri("https://github.com/nationalarchives/da-metadata-schema"),
    "git@github.com:nationalarchives/da-metadata-schema.git"
  )
)

developers := List(
  Developer(
    id = "tna-da-bot",
    name = "TNA Digital Archiving",
    email = "s-GitHubDABot@nationalarchives.gov.uk",
    url = uri("https://github.com/nationalarchives/da-metadata-schema")
  )
)

ThisBuild / description := "JSON Schema to describe The National Archives catalogue metadata"
ThisBuild / licenses := List(License("MIT", uri("https://choosealicense.com/licenses/mit/")))
ThisBuild / homepage := Some(uri("https://github.com/nationalarchives/da-metadata-schema"))
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

lazy val managedResourceDirectories = Seq("metadata-schema", "config-schema", "validation-messages", "guidance", "puids")

def copyManagedResources(configuration: Configuration) = Def.task {
  val base = baseDirectory.value
  val out = (configuration / resourceManaged).value
  managedResourceDirectories.flatMap { directory =>
    val src = base / directory
    val dest = out / directory
    IO.copyDirectory(src, dest)
    (dest ** "*").get()
  }
}

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
      ujsonLib
    ),
    Compile / resourceGenerators += copyManagedResources(Compile).taskValue,
    Test / resourceGenerators += copyManagedResources(Test).taskValue
  )

lazy val copySchema = taskKey[Unit]("copySchema")
copySchema := {
  val classesDir = (Compile / classDirectory).value
  val repoDir = baseDirectory.value
  IO.copyDirectory(repoDir / "metadata-schema", classesDir / "metadata-schema")
  IO.copyDirectory(repoDir / "config-schema", classesDir / "config-schema")
  IO.copyDirectory(repoDir / "puids", classesDir / "puids")
}

lazy val copyValidationMessageProperties = taskKey[Unit]("copyValidationMessageProperties")
copyValidationMessageProperties := {
  val classesDir = (Compile / classDirectory).value
  IO.copyDirectory(baseDirectory.value / "validation-messages", classesDir / "validation-messages")
}

lazy val copyGuidanceProperties = taskKey[Unit]("copyGuidanceProperties")
copyGuidanceProperties := {
  val classesDir = (Compile / classDirectory).value
  IO.copyDirectory(baseDirectory.value / "guidance", classesDir / "guidance")
}
