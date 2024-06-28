package uk.gov.tna.metadata.schema.validator

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.networknt.schema.*
import org.scalatest.wordspec.AnyWordSpec

import java.io.InputStream

trait BaseSpec extends AnyWordSpec {

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
