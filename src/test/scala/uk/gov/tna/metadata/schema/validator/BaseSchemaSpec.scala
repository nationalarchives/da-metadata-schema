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
      errors.asScala.toArray.map(_.getMessage) shouldBe empty
    }

    "pass when original_identifier is null" in {
      val schemaPath = "metadata-schema/baseSchema.schema.json"
      val testDataPath = "/data/testDataClosurePeriod.json"
      val withValidClosurePeriod = loadAndModifyTestData(testDataPath, "\"closure_period\": \"CLOSURE_PERIOD\"", "\"closure_period\": [1,150]")
      val modifiedData = withValidClosurePeriod.replace("\"title_alternate\": \"alternative title\"", "\"title_alternate\": \"alternative title\",\n  \"original_identifier\": null")
      val schemaSetup = createTheSchema(schemaPath)

      val errors: util.Set[ValidationMessage] = schemaSetup.validate(modifiedData, InputFormat.JSON)
      errors.asScala.toArray.map(_.getMessage) shouldBe empty
    }

    "pass when original_identifier is No Original Found" in {
      val schemaPath = "metadata-schema/baseSchema.schema.json"
      val testDataPath = "/data/testDataClosurePeriod.json"
      val withValidClosurePeriod = loadAndModifyTestData(testDataPath, "\"closure_period\": \"CLOSURE_PERIOD\"", "\"closure_period\": [1,150]")
      val modifiedData = withValidClosurePeriod.replace("\"title_alternate\": \"alternative title\"", "\"title_alternate\": \"alternative title\",\n  \"original_identifier\": \"No Original Found\"")
      val schemaSetup = createTheSchema(schemaPath)

      val errors: util.Set[ValidationMessage] = schemaSetup.validate(modifiedData, InputFormat.JSON)
      errors.size() shouldBe 0
    }

    "fail when original_identifier contains a line break" in {
      val schemaPath = "metadata-schema/baseSchema.schema.json"
      val testDataPath = "/data/testDataClosurePeriod.json"
      val withValidClosurePeriod = loadAndModifyTestData(testDataPath, "\"closure_period\": \"CLOSURE_PERIOD\"", "\"closure_period\": [1,150]")
      val modifiedData = withValidClosurePeriod.replace("\"title_alternate\": \"alternative title\"", "\"title_alternate\": \"alternative title\",\n  \"original_identifier\": \"line1\\nline2\"")
      val schemaSetup = createTheSchema(schemaPath)

      val errors: util.Set[ValidationMessage] = schemaSetup.validate(modifiedData, InputFormat.JSON)
      errors.asScala.toArray.map(_.getMessage) should contain("$.original_identifier: does not match the regex pattern ^(No Original Found|[^\\r\\n]*)$")
    }

    "fail when original_identifier exceeds 500 characters" in {
      val schemaPath = "metadata-schema/baseSchema.schema.json"
      val testDataPath = "/data/testDataClosurePeriod.json"
      val withValidClosurePeriod = loadAndModifyTestData(testDataPath, "\"closure_period\": \"CLOSURE_PERIOD\"", "\"closure_period\": [1,150]")
      val tooLongIdentifier = "x" * 501
      val modifiedData = withValidClosurePeriod.replace("\"title_alternate\": \"alternative title\"", s"\"title_alternate\": \"alternative title\",\n  \"original_identifier\": \"$tooLongIdentifier\"")
      val schemaSetup = createTheSchema(schemaPath)

      val errors: util.Set[ValidationMessage] = schemaSetup.validate(modifiedData, InputFormat.JSON)
      errors.asScala.toArray.map(_.getMessage) should contain("$.original_identifier: must be at most 500 characters long")
    }
  }
}
