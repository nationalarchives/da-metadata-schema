package uk.gov.tna.metadata.schema.validator

import com.networknt.schema.*
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec

import java.util

class RelationshipSchemaSpec extends BaseSpec {
  "relationship schema validation" should {
    "fail if missing required property" in {
      val schemaPath = "metadata-schema/relationshipSchema.schema.json"
      val testDataPath = "/data/relationship.json"
      val schemaSetup = createSchema(schemaPath, testDataPath)

      val errors: util.Set[ValidationMessage] = schemaSetup._1.validate(schemaSetup._2.toPrettyString, InputFormat.JSON)
      errors.size() shouldBe 2
      errors.iterator().next().getMessage shouldBe "$: required property 'file_name_translation_language' not found"
    }
  }
}
