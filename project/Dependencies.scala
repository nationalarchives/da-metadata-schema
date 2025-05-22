import sbt._

object Dependencies {
  lazy val commonsLang3 = "org.apache.commons" % "commons-lang3" % "3.15.0"
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.18"
  lazy val jsonSchemaValidator = "com.networknt" % "json-schema-validator" % "1.5.1"
  lazy val catsEffect = "org.typelevel" %% "cats-effect" % "3.6.1"
  lazy val circeCore = "io.circe" %% "circe-core" % "0.14.13"
  lazy val circeGeneric = "io.circe" %% "circe-generic" % "0.14.13"
  lazy val circeParser = "io.circe" %% "circe-parser" % "0.14.13"
  lazy val ujson = "com.lihaoyi" % "ujson_native0.5_2.13" % "4.1.0"

}
