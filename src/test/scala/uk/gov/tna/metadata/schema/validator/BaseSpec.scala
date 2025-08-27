package uk.gov.tna.metadata.schema.validator

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.networknt.schema._
import org.scalatest.wordspec.AnyWordSpec

import java.io.{File, InputStream}
import java.nio.file.{Files, Paths}
import scala.io.Source
import scala.util.Using

trait BaseSpec extends AnyWordSpec {

  private val projectRootDir = System.getProperty("user.dir")
  private val projectSchemaDir = Paths.get(projectRootDir, "metadata-schema").toString

  def createTheSchema(schemaPath: String): JsonSchema = {
    val fullPath = if (schemaPath.startsWith("metadata-schema/")) {
      Paths.get(projectRootDir, schemaPath).toString
    } else {
      schemaPath
    }
    val schemaInputStream = Files.newInputStream(new File(fullPath).toPath)
    val schema = getJsonSchemaFromStreamContentV7(schemaInputStream)
    schema
  }

  def createSchema(schemaPath: String, testDataPath: String): (JsonSchema, JsonNode) = {
    val fullPath = if (schemaPath.startsWith("metadata-schema/")) {
      Paths.get(projectRootDir, schemaPath).toString
    } else {
      schemaPath
    }
    val schemaInputStream = Files.newInputStream(new File(fullPath).toPath)
    val schema = getJsonSchemaFromStreamContentV7(schemaInputStream)
    val dataInputStream = getClass.getResourceAsStream(testDataPath)
    val node = getJsonNodeFromStreamContent(dataInputStream)
    (schema, node)
  }
  
  def getJsonSchemaFromStreamContentV7(schemaContent: InputStream): JsonSchema = {
    val schemaIri = SchemaId.V7
    val metaSchema = JsonMetaSchema.getV7

    val factory = new JsonSchemaFactory.Builder().defaultMetaSchemaIri(schemaIri).metaSchema(metaSchema).build()

    val config = SchemaValidatorsConfig.builder()
      .pathType(PathType.LEGACY)  // Keep LEGACY path type as in the constructor
      .formatAssertionsEnabled(true)
      .build()

    factory.getSchema(schemaContent, config)
  }

  def loadAndModifyTestData(testDataPath: String, replace: String, replacement: String): String = {
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

  protected def getJsonNodeFromStreamContent(content: InputStream): JsonNode = {
    val mapper = new ObjectMapper()
    mapper.readTree(content)
  }
}
