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

      errorsArray(0).getMessage shouldBe "$: required property 'foi_exemption_code' not found"
      errorsArray(1).getMessage shouldBe "$: required property 'foi_exemption_asserted' not found"
      errorsArray(2).getMessage shouldBe "$: required property 'description_closed' not found"
      errorsArray(3).getMessage shouldBe "$: required property 'title_alternate' not found"
    }

    "fail when document is closed and provided closure property is invalid when using closureSchemaClosed" in {
      val schemaPath = "metadata-schema/closureSchemaClosed.schema.json"
      val testDataPath = "/data/testDataClosedInvalid.json"
      val schemaSetup = createSchema(schemaPath, testDataPath)

      val errors: util.Set[ValidationMessage] = schemaSetup._1.validate(schemaSetup._2.toPrettyString, InputFormat.JSON)
      val errorsArray = errors.asScala.toArray

      errorsArray(0).getMessage shouldBe "$.closure_start_date: does not match the date pattern must be a valid RFC 3339 full-date"
      errorsArray(1).getMessage shouldBe "$.closure_period: must have a minimum value of 1"
      errorsArray(2).getMessage shouldBe "$.foi_exemption_code[2]: does not have a value in the enumeration [23, 24, 26, 27(1), 27(2), 28, 29, 30(1), 30(2), 31, 32, 33, 34, 35(1)(a), 35(1)(b), 35(1)(c), 35(1)(d), 36, 37(1)(a), 37(1)(aa), 37(1)(ac), 37(1)(ad), 37(1)(b), 38, 39, 40(2), 41, 42, 43, 43(1), 43(2), 44, EIRs 12(3) & 13, EIR 12(5)(a), EIR 12(5)(b), EIR 12(5)(c), EIR 12(5)(d), EIR 12(5)(e), EIR 12(5)(f), EIR 12(5)(g)]"
      errorsArray(3).getMessage shouldBe "$.foi_exemption_asserted: does not match the date pattern must be a valid RFC 3339 full-date"
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

    "fail when document is open and missing closure property when using closureSchemaOpen" in {
      val schemaPath = "metadata-schema/closureSchemaOpen.schema.json"
      val testDataPath = "/data/testDataOpenMissing.json"
      val schemaSetup = createSchema(schemaPath, testDataPath)

      val errors: util.Set[ValidationMessage] = schemaSetup._1.validate(schemaSetup._2.toPrettyString, InputFormat.JSON)
      val errorsArray = errors.asScala.toArray

      errorsArray(0).getMessage shouldBe "$: required property 'closure_start_date' not found"
      errorsArray(1).getMessage shouldBe "$: required property 'closure_period' not found"
      errorsArray(2).getMessage shouldBe "$: required property 'foi_exemption_code' not found"
      errorsArray(3).getMessage shouldBe "$: required property 'foi_exemption_asserted' not found"
      errorsArray(4).getMessage shouldBe "$: required property 'title_alternate' not found"
      errorsArray(5).getMessage shouldBe "$: required property 'description_closed' not found"
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
      errorsArray(6).getMessage shouldBe "$.title_closed: does not have a value in the enumeration [null, NO, No, no, false]"
      errorsArray(7).getMessage shouldBe "$.description_closed: does not have a value in the enumeration [null, NO, No, no, false]"
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
