import sbt._

object Dependencies {
  lazy val commonsLang3 = "org.apache.commons" % "commons-lang3" % "3.18.0"
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.19"
  lazy val jsonSchemaValidator = "com.networknt" % "json-schema-validator" % "1.5.8"
  lazy val catsEffect = "org.typelevel" %% "cats-effect" % "3.6.3"
  lazy val circeCore = "io.circe" %% "circe-core" % "0.14.14"
  lazy val circeGeneric = "io.circe" %% "circe-generic" % "0.14.14"
  lazy val circeGenericExtras = "io.circe" %% "circe-generic-extras" % "0.14.4"
  lazy val circeParser = "io.circe" %% "circe-parser" % "0.14.14"
  lazy val ujson = "com.lihaoyi" % "ujson_native0.5_2.13" % "4.2.1"

}
