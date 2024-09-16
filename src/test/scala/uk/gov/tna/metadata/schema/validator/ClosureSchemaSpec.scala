package uk.gov.tna.metadata.schema.validator

import com.networknt.schema.*
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec

import java.util
import scala.jdk.CollectionConverters.*

class ClosureSchemaSpec extends BaseSpec {
  "closure schema validation" should {
    "fail when document is closed and missing closure property when using closureSchemaClosed" in {
      val schemaPath = "metadata-schema/closureSchemaClosed.schema.json"
      val testDataPath = "/data/testDataClosedMissing.json"
      val schemaSetup = createSchema(schemaPath, testDataPath)

      val errors: util.Set[ValidationMessage] = schemaSetup._1.validate(schemaSetup._2.toPrettyString, InputFormat.JSON)
      val errorsArray = errors.asScala.toArray

      errorsArray(0).getMessage shouldBe "$: required property 'title_alternate' not found"
    }

    "fail when document is closed and provided closure property is invalid when using closureSchemaClosed" in {
      val schemaPath = "metadata-schema/closureSchemaClosed.schema.json"
      val testDataPath = "/data/testDataClosedInvalid.json"
      val schemaSetup = createSchema(schemaPath, testDataPath)

      val errors: util.Set[ValidationMessage] = schemaSetup._1.validate(schemaSetup._2.toPrettyString, InputFormat.JSON)
      val errorsArray = errors.asScala.toArray

      errorsArray(0).getMessage shouldBe "$.closure_start_date: integer found, string expected"
      errorsArray(1).getMessage shouldBe "$.closure_period: string found, integer expected"
      errorsArray(2).getMessage shouldBe "$.foi_exemption_code: string found, array expected"
      errorsArray(3).getMessage shouldBe "$.foi_exemption_asserted: integer found, string expected"
      errorsArray(4).getMessage shouldBe "$.description_closed: string found, boolean expected"
      errorsArray(5).getMessage shouldBe "$.title_alternate: integer found, string expected"
    }

    "succeed when document is closed and valid closure property is provided when using closureSchemaClosed" in {
      val schemaPath = "metadata-schema/closureSchemaClosed.schema.json"
      val testDataPath = "/data/testDataClosedValid.json"
      val schemaSetup = createSchema(schemaPath, testDataPath)

      val errors: Seq[String] = schemaSetup._1
        .validate(schemaSetup._2.toPrettyString, InputFormat.JSON)
        .asScala
        .map(_.getMessage)
        .toSeq

      errors.size shouldBe 0
    }

    "fail when document is open and provided closure property is invalid when using closureSchemaOpen" in {
      val schemaPath = "metadata-schema/closureSchemaOpen.schema.json"
      val testDataPath = "/data/testDataOpenInvalid.json"
      val schemaSetup = createSchema(schemaPath, testDataPath)

      val errors: util.Set[ValidationMessage] = schemaSetup._1.validate(schemaSetup._2.toPrettyString, InputFormat.JSON)
      val errorsArray = errors.asScala.toArray

      errorsArray(0).getMessage shouldBe "$.closure_start_date: string found, null expected"
      errorsArray(1).getMessage shouldBe "$.closure_period: integer found, null expected"
      errorsArray(2).getMessage shouldBe "$.foi_exemption_code: array found, null expected"
      errorsArray(3).getMessage shouldBe "$.foi_exemption_asserted: string found, null expected"
      errorsArray(4).getMessage shouldBe "$.title_alternate: string found, null expected"
      errorsArray(5).getMessage shouldBe "$.description_alternate: string found, null expected"
      errorsArray(6).getMessage shouldBe "$.title_closed: string found, boolean expected"
      errorsArray(7).getMessage shouldBe "$.description_closed: string found, boolean expected"
    }

    "succeed when document is open and valid closure property is provided when using closureSchemaOpen" in {
      val schemaPath = "metadata-schema/closureSchemaOpen.schema.json"
      val testDataPath = "/data/testDataOpenValid.json"
      val schemaSetup = createSchema(schemaPath, testDataPath)

      val errors: Seq[String] = schemaSetup._1
        .validate(schemaSetup._2.toPrettyString, InputFormat.JSON)
        .asScala
        .map(_.getMessage)
        .toSeq

      errors.size shouldBe 0
    }
  }
}
