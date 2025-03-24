package uk.gov.tna.metadata.schema.validator

import com.networknt.schema.*
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec

import java.util
import scala.jdk.CollectionConverters.*

class RelationshipSchemaSpec extends BaseSpec {
  "relationship schema validation" should {
    "fail if missing required property" in {
      val schemaPath = "metadata-schema/relationshipSchema.schema.json"
      val testDataPath = "/data/relationship.json"
      val schemaSetup = createSchema(schemaPath, testDataPath)

      val errors: util.Set[ValidationMessage] = schemaSetup._1.validate(schemaSetup._2.toPrettyString, InputFormat.JSON)
      assert(errors.size == 1)
      val errorsList = errors.asScala.toList
      assert(errorsList.head.getMessage == "$: required property 'description' not found")
    }
  }
}
