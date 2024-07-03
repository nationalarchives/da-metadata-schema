package uk.gov.tna.metadata.schema.validator

import com.networknt.schema.*
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec

import java.util
import scala.jdk.CollectionConverters.*

class ClosureSchemaSpec extends BaseSpec {
  "closure schema validation" should {
    "fail when document is closed and missing closure property when using closureSchema" in {
      val schemaPath = "metadata-schema/closureSchema.schema.json"
      val testDataPath = "/data/testDataClosed.json"
      val schemaSetup = createSchema(schemaPath, testDataPath)

      val errors: util.Set[ValidationMessage] = schemaSetup._1.validate(schemaSetup._2.toPrettyString, InputFormat.JSON)
      val errorsArray = errors.asScala.toArray

      errorsArray(0).getMessage shouldBe "$.foi_exemption_code[2]: does not have a value in the enumeration [23, 24, 26, 27(1), 27(2), 28, 29, 30(1), 30(2), 31, 32, 33, 34, 35(1)(a), 35(1)(b), 35(1)(c), 35(1)(d), 36, 37(1)(a), 37(1)(aa), 37(1)(ac), 37(1)(ad), 37(1)(b), 38, 39, 40(2), 41, 42, 43, 43(1), 43(2), 44, EIRs 12(3) & 13, EIR 12(5)(a), EIR 12(5)(b), EIR 12(5)(c), EIR 12(5)(d), EIR 12(5)(e), EIR 12(5)(f), EIR 12(5)(g)]"
      errorsArray(1).getMessage shouldBe "$: required property 'description_closed' not found"
    }

    "pass an open document when using closureSchema" in {
      val schemaPath = "metadata-schema/closureSchema.schema.json"
      val testDataPath = "/data/testDataOpen.json"
      val schemaSetup = createSchema(schemaPath, testDataPath)

      val errors: util.Set[ValidationMessage] = schemaSetup._1.validate(schemaSetup._2.toPrettyString, InputFormat.JSON)

      errors.size() shouldBe 0
    }
  }
}
