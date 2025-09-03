package uk.gov.tna.metadata.schema.validator

import com.networknt.schema._
import org.scalatest.matchers.should.Matchers._

import java.util
import scala.jdk.CollectionConverters._

class RelationshipSchemaSpec extends BaseSpec {
  "relationship schema validation" should {
    "fail if missing required property when alternate property has value" in {
      val schemaPath = "metadata-schema/relationshipSchema.schema.json"
      val testDataPath = "/data/relationship.json"
      val schemaSetup = createSchema(schemaPath, testDataPath)

      val errors: util.Set[ValidationMessage] = schemaSetup._1.validate(schemaSetup._2.toPrettyString, InputFormat.JSON)
      assert(errors.size == 1)
      val errorsList = errors.asScala.toList
      assert(errorsList.head.getMessage == "$: required property 'description' not found")
    }

    "pass with valid judgment data" in {
      val schemaPath = "metadata-schema/relationshipSchema.schema.json"
      val testDataPath = "/data/testDataJudgment.json"
      val modifiedData = loadAndModifyTestData(testDataPath, "\"UPDATE_TYPE\"", "[\"Typo\",\"Amendment to NCN\"]")
      val schemaSetup = createTheSchema(schemaPath)

      val errors: util.Set[ValidationMessage] = schemaSetup.validate(modifiedData, InputFormat.JSON)
      errors.size() shouldBe 0
    }
    "fail when incorrect value in update type" in {
      val schemaPath = "metadata-schema/baseSchema.schema.json"
      val testDataPath = "/data/testDataJudgment.json"
      val modifiedData = loadAndModifyTestData(testDataPath, "\"UPDATE_TYPE\"", "[\"Typos\",\"Amendment to NCN\"]")
      val schemaSetup = createTheSchema(schemaPath)

      val errors: util.Set[ValidationMessage] = schemaSetup.validate(modifiedData, InputFormat.JSON)
      val errorsArray = errors.asScala.toArray

      errorsArray.head.getMessage shouldBe "$.judgment_update_type[0]: does not have a value in the enumeration [\"Typo\", \"Formatting\", \"Amendment to NCN\", \"Other\", \"Anonymisation/redaction\"]"
    }

    "fail when no judgment_type" in {
      val schemaPath = "metadata-schema/requiredJudgmentSchema.json"
      val testDataPath = "/data/testDataJudgment.json"
      val modifiedData = loadAndModifyTestData(testDataPath, "\"judgment_type\"", "\"judgment_types\"")
      val schemaSetup = createTheSchema(schemaPath)

      val errors: util.Set[ValidationMessage] = schemaSetup.validate(modifiedData, InputFormat.JSON)
      val errorsArray = errors.asScala.toArray

      errorsArray.head.getMessage shouldBe "$: required property 'judgment_type' not found"
    }
    "fail when judgment_update is true and update type empty string" in {
      val schemaPath = "metadata-schema/relationshipSchema.schema.json"
      val testDataPath = "/data/testDataJudgment.json"
      val modifiedData = loadAndModifyTestData(testDataPath, "\"UPDATE_TYPE\"", "\"\"")
      val schemaSetup = createTheSchema(schemaPath)

      val errors: util.Set[ValidationMessage] = schemaSetup.validate(modifiedData, InputFormat.JSON)
      val errorsArray = errors.asScala.toArray

      errorsArray.length shouldBe 1
      val judgmentUpdateTypeError = errorsArray.find(_.getMessage.contains("judgment_update_type"))
      judgmentUpdateTypeError shouldBe defined
      judgmentUpdateTypeError.get.getMessage should include("string found, array expected")
    }
    "fail when judgment_update is true and update type empty array" in {
      val schemaPath = "metadata-schema/relationshipSchema.schema.json"
      val testDataPath = "/data/testDataJudgment.json"
      val modifiedData = loadAndModifyTestData(testDataPath, "\"UPDATE_TYPE\"", "[]")
      val schemaSetup = createTheSchema(schemaPath)

      val errors: util.Set[ValidationMessage] = schemaSetup.validate(modifiedData, InputFormat.JSON)
      val errorsArray = errors.asScala.toArray

      errorsArray.head.getMessage shouldBe "$.judgment_update_type: must have at least 1 items but found 0"
    }
    "pass when judgment_no_neutral_citation is false and judgment_neutral_citation is empty" in {
      val schemaPath = "metadata-schema/relationshipSchema.schema.json"
      val testDataPath = "/data/testDataJudgment.json"
      val modifiedData = loadAndModifyTestData(testDataPath, "\"UPDATE_TYPE\"", "[\"Typo\",\"Amendment to NCN\"]")
      val schemaSetup = createTheSchema(schemaPath)

      val errors: util.Set[ValidationMessage] = schemaSetup.validate(modifiedData, InputFormat.JSON)
      errors.size shouldBe 0
    }
    "pass test" in {
      val schemaPath = "metadata-schema/relationshipSchema.schema.json"
      //val schemaPath = "metadata-schema/baseSchema.schema.json"
      val modifiedData = """
        {
          "judgment_neutral_citation": "gg",
          "judgment_no_neutral_citation": false,
          "related_title": "some title"
        }
        """"""
      val schemaSetup = createTheSchema(schemaPath)

      val errors: util.Set[ValidationMessage] = schemaSetup.validate(modifiedData, InputFormat.JSON)
      println(errors)
      errors.size() shouldBe 0
    }
  }
}
