package uk.gov.tna.metadata.schema.validator

import com.networknt.schema.{InputFormat, ValidationMessage}
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec

import java.io.InputStream
import java.util
import scala.io.Source
import scala.jdk.CollectionConverters.*
import scala.util.Using

class BaseSchemaSpec extends BaseSpec {

  "Base schema validation" should {

    "fail when closure_period is a string" in {
      val schemaPath = "metadata-schema/baseSchema.schema.json"
      val testDataPath = "/data/testDataClosurePeriod.json"
      val modifiedData = loadAndModifyTestData(testDataPath, "CLOSURE_PERIOD", "3")
      val schemaSetup = createTheSchema(schemaPath)

      val errors: util.Set[ValidationMessage] = schemaSetup.validate(modifiedData, InputFormat.JSON)
      val errorsArray = errors.asScala.toArray

      errorsArray.head.getMessage shouldBe "$.closure_period: string found, [array, null] expected"
    }

    "fail when closure_period has a value above the maximum limit" in {
      val schemaPath = "metadata-schema/baseSchema.schema.json"
      val testDataPath = "/data/testDataClosurePeriod.json"
      val modifiedData = loadAndModifyTestData(testDataPath, "\"CLOSURE_PERIOD\"", "[1,160]")
      val schemaSetup = createTheSchema(schemaPath)

      val errors: util.Set[ValidationMessage] = schemaSetup.validate(modifiedData, InputFormat.JSON)
      val errorsArray = errors.asScala.toArray

      errorsArray.head.getMessage shouldBe "$.closure_period[1]: must have a maximum value of 150"
    }
  }

  private def loadAndModifyTestData(testDataPath: String, replace: String, replacement: String): String = {
    Option(getClass.getResourceAsStream(testDataPath)) match {
      case Some(inputStream) =>
        Using.resource(inputStream) { stream =>
          val jsonString = Source.fromInputStream(stream).mkString
          jsonString.replace(replace, replacement)
        }
      case None =>
        throw new IllegalArgumentException(s"Resource not found: $testDataPath")
    }
  }
}
