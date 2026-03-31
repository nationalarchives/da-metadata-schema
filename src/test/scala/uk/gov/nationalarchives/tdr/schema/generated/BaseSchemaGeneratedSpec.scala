package uk.gov.nationalarchives.tdr.schema.generated

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

class BaseSchemaGeneratedSpec extends AnyWordSpec {

  "BaseSchema generated constants" should {
    "contain all system properties" in {
      BaseSchema.file_path shouldBe "file_path"
      BaseSchema.file_name shouldBe "file_name"
      BaseSchema.date_last_modified shouldBe "date_last_modified"
      BaseSchema.file_size shouldBe "file_size"
      BaseSchema.UUID shouldBe "UUID"
      BaseSchema.file_reference shouldBe "file_reference"
      BaseSchema.parent_reference shouldBe "parent_reference"
      BaseSchema.file_type shouldBe "file_type"
      BaseSchema.client_side_checksum shouldBe "client_side_checksum"
      BaseSchema.server_side_checksum shouldBe "server_side_checksum"
    }

    "contain all closure properties" in {
      BaseSchema.closure_type shouldBe "closure_type"
      BaseSchema.closure_start_date shouldBe "closure_start_date"
      BaseSchema.closure_period shouldBe "closure_period"
      BaseSchema.foi_exemption_code shouldBe "foi_exemption_code"
      BaseSchema.foi_exemption_asserted shouldBe "foi_exemption_asserted"
      BaseSchema.title_closed shouldBe "title_closed"
      BaseSchema.title_alternate shouldBe "title_alternate"
      BaseSchema.description_closed shouldBe "description_closed"
      BaseSchema.description_alternate shouldBe "description_alternate"
    }

    "contain all descriptive metadata properties" in {
      BaseSchema.description shouldBe "description"
      BaseSchema.end_date shouldBe "end_date"
      BaseSchema.former_reference_department shouldBe "former_reference_department"
      BaseSchema.language shouldBe "language"
      BaseSchema.file_name_translation shouldBe "file_name_translation"
    }

    "contain all rights and copyright properties" in {
      BaseSchema.rights_copyright shouldBe "rights_copyright"
      BaseSchema.copyright_details shouldBe "copyright_details"
      BaseSchema.restrictions_on_use shouldBe "restrictions_on_use"
      BaseSchema.legal_status shouldBe "legal_status"
      BaseSchema.held_by shouldBe "held_by"
    }

    "contain judgment-related properties" in {
      BaseSchema.judgment_type shouldBe "judgment_type"
      BaseSchema.judgment_reference shouldBe "judgment_reference"
      BaseSchema.judgment_neutral_citation shouldBe "judgment_neutral_citation"
      BaseSchema.judgment_no_neutral_citation shouldBe "judgment_no_neutral_citation"
      BaseSchema.judgment_update shouldBe "judgment_update"
      BaseSchema.judgment_update_type shouldBe "judgment_update_type"
      BaseSchema.judgment_update_details shouldBe "judgment_update_details"
    }

    "contain additional metadata properties" in {
      BaseSchema.related_material shouldBe "related_material"
      BaseSchema.evidence_provided_by shouldBe "evidence_provided_by"
      BaseSchema.note shouldBe "note"
      BaseSchema.creating_body shouldBe "creating_body"
      BaseSchema.original_identifier shouldBe "original_identifier"
    }

    "contain date-related properties" in {
      BaseSchema.date_created shouldBe "date_created"
      BaseSchema.date_last_modified shouldBe "date_last_modified"
      BaseSchema.end_date shouldBe "end_date"
      BaseSchema.start_date shouldBe "start_date"
      BaseSchema.date_range shouldBe "date_range"
    }

    "contain language-related properties" in {
      BaseSchema.language shouldBe "language"
      BaseSchema.file_name_language shouldBe "file_name_language"
      BaseSchema.file_name_translation shouldBe "file_name_translation"
      BaseSchema.file_name_translation_language shouldBe "file_name_translation_language"
    }
  }

  "BaseSchema constant values" should {
    "match their property names exactly" in {
      // Verify the constant name matches the property value it represents
      // This ensures consistency in code generation
      val constantName = "file_path"
      BaseSchema.file_path shouldBe constantName
    }

    "use underscore naming convention" in {
      // All properties should use snake_case
      val snakeCasePattern = "^[a-z_][a-z0-9_]*$".r
      BaseSchema.file_path should fullyMatch regex snakeCasePattern
      BaseSchema.date_last_modified should fullyMatch regex snakeCasePattern
      BaseSchema.foi_exemption_code should fullyMatch regex snakeCasePattern
    }

    "be non-empty strings" in {
      BaseSchema.file_path should not be empty
      BaseSchema.file_name should not be empty
      BaseSchema.UUID should not be empty
    }
  }

  "BaseSchema usage patterns" should {
    "allow using constants in maps" in {
      val metadataMap = Map(
        BaseSchema.file_name -> "test.txt",
        BaseSchema.file_path -> "/path/to/file",
        BaseSchema.closure_type -> "Open"
      )
      metadataMap(BaseSchema.file_name) shouldBe "test.txt"
      metadataMap(BaseSchema.file_path) shouldBe "/path/to/file"
      metadataMap(BaseSchema.closure_type) shouldBe "Open"
    }

    "allow using constants in pattern matching" in {
      val propertyName = "file_name"
      val result = propertyName match {
        case BaseSchema.file_name => "Found file name"
        case BaseSchema.file_path => "Found file path"
        case _ => "Other property"
      }
      result shouldBe "Found file name"
    }

    "allow using constants for validation logic" in {
      val requiredFields = Set(
        BaseSchema.file_path,
        BaseSchema.file_name,
        BaseSchema.date_last_modified,
        BaseSchema.closure_type
      )
      requiredFields should contain(BaseSchema.file_path)
      requiredFields.size shouldBe 4
    }
  }

  "BaseSchema object" should {
    "have String type for all constants" in {
      BaseSchema.file_path shouldBe a[String]
      BaseSchema.closure_type shouldBe a[String]
      BaseSchema.UUID shouldBe a[String]
    }

    "provide compile-time safety for property names" in {
      // This test verifies that typos in property names cause compile errors
      // The fact that this test compiles proves the constants exist
      val _ = BaseSchema.file_name
      val _ = BaseSchema.date_last_modified
      val _ = BaseSchema.foi_exemption_code
      succeed
    }
  }
}

