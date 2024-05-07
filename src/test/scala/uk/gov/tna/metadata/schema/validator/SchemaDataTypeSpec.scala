package uk.gov.tna.metadata.schema.validator

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.networknt.schema._
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

import java.io.{File, InputStream}
import java.nio.file.Files
import java.util

class SchemaDataTypeSpec extends AnyWordSpec {

  "schema validation" should {
    "fail if missing required property from relationship when using relationshipSchema" in {
      val schemaPath = "metadata-schema/relationshipSchema.schema.json"
      val dataPath = "/data/relationship.json"
      val schemaInputStream: InputStream = Files.newInputStream(new File(schemaPath).toPath)
      val schema = getJsonSchemaFromStreamContentV7(schemaInputStream)
      val dataInputStream = getClass.getResourceAsStream(dataPath)
      val node = getJsonNodeFromStreamContent(dataInputStream)

      val errors: util.Set[ValidationMessage] = schema.validate(node.toPrettyString, InputFormat.JSON)
      errors.size() shouldBe 2
      errors.iterator().next().getMessage shouldBe "$: required property 'file_name_translation_language' not found"
    }

    "fail when document is closed and missing closure property when using closureSchema" in {
      val schemaPath = "metadata-schema/closureSchema.schema.json"
      val dataPath = "/data/testData.json"
      val schemaInputStream = Files.newInputStream(new File(schemaPath).toPath)
      val schema = getJsonSchemaFromStreamContentV7(schemaInputStream)
      val dataInputStream = getClass.getResourceAsStream(dataPath)
      val node = getJsonNodeFromStreamContent(dataInputStream)

      val errors: util.Set[ValidationMessage] = schema.validate(node.toPrettyString, InputFormat.JSON)
      
      errors.iterator().next().getMessage shouldBe "$: required property 'description_closed' not found"
    }

    "pass an open document when using closureSchema" in {
      val schemaPath = "metadata-schema/closureSchema.schema.json"
      val dataPath = "/data/testDataOpen.json"
      val schemaInputStream = Files.newInputStream(new File(schemaPath).toPath)
      val schema = getJsonSchemaFromStreamContentV7(schemaInputStream)
      val dataInputStream = getClass.getResourceAsStream(dataPath)
      val node = getJsonNodeFromStreamContent(dataInputStream)

      val errors: util.Set[ValidationMessage] = schema.validate(node.toPrettyString, InputFormat.JSON)

      errors.size() shouldBe 0
    }

  }

  def getJsonSchemaFromStreamContentV7(schemaContent: InputStream): JsonSchema = {
    val schemaIri = SchemaId.V7
    val metaSchema = JsonMetaSchema.getV7

    val factory = new JsonSchemaFactory.Builder().defaultMetaSchemaIri(schemaIri).metaSchema(metaSchema).build()
    val config = new SchemaValidatorsConfig()
    config.setFormatAssertionsEnabled(true)

    factory.getSchema(schemaContent, config)
  }

  protected def getJsonNodeFromStreamContent(content: InputStream): JsonNode = {
    val mapper = new ObjectMapper()
    mapper.readTree(content)
  }
}
