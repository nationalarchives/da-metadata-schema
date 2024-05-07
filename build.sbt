import Dependencies.*
import sbt.url
import sbtrelease.ReleaseStateTransformations._

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
    id = "tna-digital-archiving-jenkins",
    name = "TNA Digital Archiving",
    email = "digitalpreservation@nationalarchives.gov.uk",
    url = url("https://github.com/nationalarchives/da-metadata-schema")
  )
)

ThisBuild / description := "A library to validate input metadata for Transfer Digital Records"
ThisBuild / licenses := List("MIT" -> new URL("https://choosealicense.com/licenses/mit/"))
ThisBuild / homepage := Some(url("https://github.com/nationalarchives/da-metadata-schema"))

useGpgPinentry := true
publishTo := sonatypePublishToBundle.value
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
      jsonSchemaValidator
    )
  )

libraryDependencies += "org.apache.pekko" %% "pekko-actor-typed" % "1.0.2"
libraryDependencies += "org.apache.pekko" %% "pekko-connectors-csv" % "1.0.2"
libraryDependencies += "org.apache.pekko" %% "pekko-stream" % "1.0.2"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.10.4"
libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.17.0"
