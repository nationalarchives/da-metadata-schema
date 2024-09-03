package uk.gov.tna.metadata.schema.validator

import com.networknt.schema.{InputFormat, ValidationMessage}
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec

import java.util
import scala.jdk.CollectionConverters.*

class SharepointDataLoadSchemaSpec extends BaseSpec {
  val schemaPath = "metadata-schema/dataLoadSharePointSchema.schema.json"

  "sharepoint data load schema validation" should {
    "pass when all required properties present" in {
      val testDataPath = "/data/sharepointDataLoad.json"
      val schemaSetup = createSchema(schemaPath, testDataPath)

      val errors: util.Set[ValidationMessage] = schemaSetup._1.validate(schemaSetup._2.toPrettyString, InputFormat.JSON)

      errors.size() shouldBe 0
    }

    "fail when missing required property" in {
      val testDataPath = "/data/sharepointDataLoadMissingProperty.json"
      val schemaSetup = createSchema(schemaPath, testDataPath)

      val errors: util.Set[ValidationMessage] = schemaSetup._1.validate(schemaSetup._2.toPrettyString, InputFormat.JSON)

      errors.size() shouldBe 1
      val errorsArray = errors.asScala.toArray
      errorsArray(0).getMessage shouldBe "$: required property 'Modified' not found"
    }

    "fail when 'File_x0020_Size' is less than 0" in {
      val testDataPath = "/data/sharepointDataLoadInvalidFileSize.json"
      val schemaSetup = createSchema(schemaPath, testDataPath)

      val errors: util.Set[ValidationMessage] = schemaSetup._1.validate(schemaSetup._2.toPrettyString, InputFormat.JSON)

      errors.size() shouldBe 1
      val errorsArray = errors.asScala.toArray
      errorsArray(0).getMessage shouldBe "$.File_x0020_Size: must have a minimum value of 0"
    }

    "fail when 'FileRef' is less than 1" in {

      val testDataPath = "/data/sharepointDataLoadInvalidFilePath.json"
      val schemaSetup = createSchema(schemaPath, testDataPath)

      val errors: util.Set[ValidationMessage] = schemaSetup._1.validate(schemaSetup._2.toPrettyString, InputFormat.JSON)

      errors.size() shouldBe 1
      val errorsArray = errors.asScala.toArray
      errorsArray(0).getMessage shouldBe "$.FileRef: must be at least 1 characters long"
    }
  }
}
