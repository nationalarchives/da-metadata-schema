import sbt.*

object Dependencies {
  lazy val commonsLang3 = "org.apache.commons" % "commons-lang3" % "3.15.0"
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.18"
  lazy val jsonSchemaValidator = "com.networknt" % "json-schema-validator" % "1.5.1"
}
