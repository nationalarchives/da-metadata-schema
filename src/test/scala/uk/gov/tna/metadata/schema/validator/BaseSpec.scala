package uk.gov.tna.metadata.schema.validator

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.networknt.schema.*
import org.scalatest.wordspec.AnyWordSpec

import java.io.{File, InputStream}
import java.nio.file.Files

trait BaseSpec extends AnyWordSpec {

  def createTheSchema(schemaPath: String): JsonSchema = {    
    val schemaInputStream = Files.newInputStream(new File(schemaPath).toPath)
    val schema = getJsonSchemaFromStreamContentV7(schemaInputStream)
    schema
  }

  def createSchema(schemaPath: String, testDataPath: String): (JsonSchema, JsonNode) = {
    val schemaInputStream = Files.newInputStream(new File(schemaPath).toPath)
    val schema = getJsonSchemaFromStreamContentV7(schemaInputStream)
    val dataInputStream = getClass.getResourceAsStream(testDataPath)
    val node = getJsonNodeFromStreamContent(dataInputStream)
    (schema, node)
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
