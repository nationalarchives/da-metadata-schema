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
      ujsonLib
    ),
    // Source generator: produce only the BaseSchema object with property name constants
    Compile / sourceGenerators += Def.task {
      import scala.io.Source
      import scala.util.Using
      import ujson.*
      val log = streams.value.log
      val schemaFile = baseDirectory.value / "metadata-schema" / "baseSchema.schema.json"
      if (!schemaFile.exists()) {
        log.warn(s"Schema file not found: $schemaFile")
        Seq.empty[File]
      } else {
        val jsonStr = Using(Source.fromFile(schemaFile)(scala.io.Codec.UTF8))(_.mkString).get
        val parsed = ujson.read(jsonStr)
        val propNames: Seq[String] = parsed.obj.get("properties") match {
          case Some(obj: ujson.Obj) => obj.value.keys.toSeq.sorted
          case _ => Seq.empty
        }
        val constantsBuilder = new StringBuilder
        propNames.foreach { n =>
          val safe = n.replaceAll("[^A-Za-z0-9_]", "_")
          constantsBuilder.append("  val ").append(safe).append(": String = \"").append(n).append("\"\n")
        }
        val sb = new StringBuilder
        sb.append("package uk.gov.nationalarchives.tdr.schema.generated\n")
        sb.append("object BaseSchema {\n")
        sb.append(constantsBuilder.toString)
        sb.append("}\n")
        val code = sb.toString
        val outDir = (Compile / sourceManaged).value / "generated"
        outDir.mkdirs()
        val outFile = outDir / "BaseSchema.scala"
        IO.write(outFile, code)
        log.info(s"Generated ${outFile.getAbsolutePath} with ${propNames.size} property constants.")
        Seq(outFile)
      }
    }.taskValue,
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
