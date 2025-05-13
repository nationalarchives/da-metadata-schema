package uk.gov.tna.metadata.schema.validator

import com.networknt.schema.*
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec

import java.util
import scala.jdk.CollectionConverters.*

class BaseSchemaSpec extends BaseSpec {
  "base schema validation" should {
    "fail closure period is string" in {
      val schemaPath = "metadata-schema/baseSchema.schema.json"
      val testDataPath = "/data/testDataClosurePeriod.json"
      val inputStream = getClass.getResourceAsStream(testDataPath)
      val jsonString = scala.io.Source.fromInputStream(inputStream).mkString
      inputStream.close()
      val data = jsonString.replace("CLOSURE_PERIOD", "3")
      val schemaSetup = createTheSchema(schemaPath)

      val errors: util.Set[ValidationMessage] = schemaSetup.validate(data, InputFormat.JSON)
      val errorsArray = errors.asScala.toArray

      errorsArray(0).getMessage shouldBe "$.closure_period: string found, [array, null] expected"
    }
    "fail closure period one value above max" in {
      val schemaPath = "metadata-schema/baseSchema.schema.json"
      val testDataPath = "/data/testDataClosurePeriod.json"
      val inputStream = getClass.getResourceAsStream(testDataPath)
      val jsonString = scala.io.Source.fromInputStream(inputStream).mkString
      inputStream.close()
      val data = jsonString.replace("\"CLOSURE_PERIOD\"", "[1,160]")
      val schemaSetup = createTheSchema(schemaPath)

      val errors: util.Set[ValidationMessage] = schemaSetup.validate(data, InputFormat.JSON)
      val errorsArray = errors.asScala.toArray

      errorsArray(0).getMessage shouldBe "$.closure_period[[1]: must have a maximum value of 150]"
    }
  }
}
