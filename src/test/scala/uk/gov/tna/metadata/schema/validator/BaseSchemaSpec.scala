package uk.gov.tna.metadata.schema.validator

import com.networknt.schema.{InputFormat, ValidationMessage}
import org.scalatest.matchers.should.Matchers._

import java.util
import scala.io.Source
import scala.jdk.CollectionConverters._
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

    "pass with valid list of periods" in {
      val schemaPath = "metadata-schema/baseSchema.schema.json"
      val testDataPath = "/data/testDataClosurePeriod.json"
      val modifiedData = loadAndModifyTestData(testDataPath, "\"CLOSURE_PERIOD\"", "[1,150]")
      val schemaSetup = createTheSchema(schemaPath)

      val errors: util.Set[ValidationMessage] = schemaSetup.validate(modifiedData, InputFormat.JSON)
      errors.size() shouldBe 0
    }
  }
}
