package uk.gov.tna.metadata.schema.validator

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.networknt.schema._
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

import java.io.{File, InputStream}
import java.nio.file.Files
import java.util
import scala.jdk.CollectionConverters._

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
      val dataPath = "/data/testDataClosed.json"
      val schemaInputStream = Files.newInputStream(new File(schemaPath).toPath)
      val schema = getJsonSchemaFromStreamContentV7(schemaInputStream)
      val dataInputStream = getClass.getResourceAsStream(dataPath)
      val node = getJsonNodeFromStreamContent(dataInputStream)

      val errors: util.Set[ValidationMessage] = schema.validate(node.toPrettyString, InputFormat.JSON)
      val errorsArray = errors.asScala.toArray

      errorsArray(0).getMessage shouldBe "$.foi_exemption_code[2]: does not have a value in the enumeration [23, 24, 26, 27(1), 27(2), 28, 29, 30(1), 30(2), 31, 32, 33, 34, 35(1)(a), 35(1)(b), 35(1)(c), 35(1)(d), 36, 37(1)(a), 37(1)(aa), 37(1)(ac), 37(1)(ad), 37(1)(b), 38, 39, 40(2), 41, 42, 43, 43(1), 43(2), 44, EIRs 12(3) & 13, EIR 12(5)(a), EIR 12(5)(b), EIR 12(5)(c), EIR 12(5)(d), EIR 12(5)(e), EIR 12(5)(f), EIR 12(5)(g)]"
      errorsArray(1).getMessage shouldBe "$: required property 'description_closed' not found"
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
