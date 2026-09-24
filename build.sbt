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
@transient lazy val generateManagedCompileResources = taskKey[Seq[File]]("Copy managed resources into the compile resource pipeline")
@transient lazy val generateManagedTestResources = taskKey[Seq[File]]("Copy managed compile resources into the test resource pipeline")

def copyManagedDirectories(from: File, to: File): Seq[File] =
  managedResourceDirectories.flatMap { directory =>
    val src = from / directory
    val dest = to / directory
    IO.copyDirectory(src, dest)
    (dest ** "*").get()
  }

def copyManagedFiles(fromRoot: File, toRoot: File, files: Seq[File]): Seq[File] =
  files.filter(_.isFile).flatMap { file =>
    IO.relativize(fromRoot, file).map { relativePath =>
      val target = toRoot / relativePath
      IO.createDirectory(target.getParentFile)
      IO.copyFile(file, target, preserveLastModified = true)
      target
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
    generateManagedCompileResources := copyManagedDirectories(baseDirectory.value, (Compile / resourceManaged).value),
    generateManagedTestResources := {
      copyManagedFiles((Compile / resourceManaged).value, (Test / resourceManaged).value, (Compile / managedResources).value)
    },
    Compile / resourceGenerators += generateManagedCompileResources.taskValue,
    Test / resourceGenerators += generateManagedTestResources.taskValue
  )
