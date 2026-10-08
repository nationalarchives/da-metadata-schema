import sbt._

object Dependencies {
  private val circeVersion = "0.14.17"

  lazy val commonsLang3 = "org.apache.commons" % "commons-lang3" % "3.21.0"
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.20"
  lazy val jsonSchemaValidator = "com.networknt" % "json-schema-validator" % "1.5.8"
  lazy val catsEffect = "org.typelevel" %% "cats-effect" % "3.7.1"
  lazy val circeCore = "io.circe" %% "circe-core" % circeVersion
  lazy val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
  lazy val circeGenericExtras = "io.circe" %% "circe-generic-extras" % "0.14.4"
  lazy val circeParser = "io.circe" %% "circe-parser" % circeVersion
  lazy val ujsonLib = "com.lihaoyi" %% "ujson" % "4.4.3"
}
