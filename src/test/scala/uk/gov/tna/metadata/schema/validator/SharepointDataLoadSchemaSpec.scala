package uk.gov.tna.metadata.schema.validator

import com.networknt.schema.{InputFormat, ValidationMessage}
import scala.jdk.CollectionConverters.*
import java.util
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec

import java.io.File
import java.nio.file.Files

class SharepointDataLoadSchemaSpec extends BaseSpec {

  "sharepoint data load schema validation" should {
    "pass when all required properties present" in {
      val schemaPath = "metadata-schema/dataLoadSharePointSchema.schema.json"
      val dataPath = "/data/sharepointDataLoad.json"
      val schemaInputStream = Files.newInputStream(new File(schemaPath).toPath)
      val schema = getJsonSchemaFromStreamContentV7(schemaInputStream)
      val dataInputStream = getClass.getResourceAsStream(dataPath)
      val node = getJsonNodeFromStreamContent(dataInputStream)

      val errors: util.Set[ValidationMessage] = schema.validate(node.toPrettyString, InputFormat.JSON)

      errors.size() shouldBe 0
    }

    "fail when missing required property" in {
      val schemaPath = "metadata-schema/dataLoadSharePointSchema.schema.json"
      val dataPath = "/data/sharepointDataLoadMissingProperty.json"
      val schemaInputStream = Files.newInputStream(new File(schemaPath).toPath)
      val schema = getJsonSchemaFromStreamContentV7(schemaInputStream)
      val dataInputStream = getClass.getResourceAsStream(dataPath)
      val node = getJsonNodeFromStreamContent(dataInputStream)

      val errors: util.Set[ValidationMessage] = schema.validate(node.toPrettyString, InputFormat.JSON)

      errors.size() shouldBe 1
      val errorsArray = errors.asScala.toArray
      errorsArray(0).getMessage shouldBe "$: required property 'date_last_modified' not found"
    }

    "fail when 'file_size' is less than 0" in {
      val schemaPath = "metadata-schema/dataLoadSharePointSchema.schema.json"
      val dataPath = "/data/sharepointDataLoadInvalidFileSize.json"
      val schemaInputStream = Files.newInputStream(new File(schemaPath).toPath)
      val schema = getJsonSchemaFromStreamContentV7(schemaInputStream)
      val dataInputStream = getClass.getResourceAsStream(dataPath)
      val node = getJsonNodeFromStreamContent(dataInputStream)

      val errors: util.Set[ValidationMessage] = schema.validate(node.toPrettyString, InputFormat.JSON)

      errors.size() shouldBe 1
      val errorsArray = errors.asScala.toArray
      errorsArray(0).getMessage shouldBe "$.file_size: must have a minimum value of 0"
    }

    "fail when 'file_path' is less than 1" in {
      val schemaPath = "metadata-schema/dataLoadSharePointSchema.schema.json"
      val dataPath = "/data/sharepointDataLoadInvalidFilePath.json"
      val schemaInputStream = Files.newInputStream(new File(schemaPath).toPath)
      val schema = getJsonSchemaFromStreamContentV7(schemaInputStream)
      val dataInputStream = getClass.getResourceAsStream(dataPath)
      val node = getJsonNodeFromStreamContent(dataInputStream)

      val errors: util.Set[ValidationMessage] = schema.validate(node.toPrettyString, InputFormat.JSON)

      errors.size() shouldBe 1
      val errorsArray = errors.asScala.toArray
      errorsArray(0).getMessage shouldBe "$.file_path: must be at least 1 characters long"
    }
  }
}
