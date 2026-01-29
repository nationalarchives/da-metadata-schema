package uk.gov.nationalarchives.tdr.schemautils

import com.fasterxml.jackson.databind.ObjectMapper
import io.circe.generic.auto._
import io.circe.jawn.decode
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.nationalarchives.tdr.schemautils.ConfigUtils.{Config, ConfigItem}

import scala.io.Source
import scala.util.Using

class ConfigUtilsSpec extends AnyWordSpec {

  private def withEnvironment[T](env: String)(block: => T): T = {
    val originalEnv = sys.props.get("ENVIRONMENT")
    sys.props("ENVIRONMENT") = env
    val result = block
    originalEnv match {
      case Some(value) => sys.props("ENVIRONMENT") = value
      case None => sys.props.remove("ENVIRONMENT")
    }
    result
  }

  "config.json" should {
    val nodeSchema = Using(Source.fromResource("config-schema/config.json"))(_.mkString)
    val mapper = new ObjectMapper()
    val configData = mapper.readTree(nodeSchema.get).toPrettyString
    val propertyKeys = decode[Config](configData)
      .getOrElse(Config(List.empty[ConfigItem])).configItems.map(_.key)

    "contain the correct number of properties" in {
      propertyKeys.size should equal(44)
    }

    "not contain duplicate properties" in {
      val numberOfProperties = propertyKeys.size
      numberOfProperties shouldNot equal(0)
      numberOfProperties should equal(propertyKeys.toSet.size)
    }

    "only reference properties from base schema" in {
      val baseSchemaPathPropertiesPath = "classpath:/metadata-schema/baseSchema.schema.json#/properties"
      case class BaseSchemaRef(key: String, $ref: String)
      case class BaseSchemaReferences(configItems: List[BaseSchemaRef])
      val items = decode[BaseSchemaReferences](configData).getOrElse(BaseSchemaReferences(List.empty[BaseSchemaRef]))
      items.configItems.size shouldNot equal(0)
      items.configItems.foreach(
        i => i.$ref should equal(s"$baseSchemaPathPropertiesPath/${i.key}"))
    }
  }

  "ConfigUtils" should {
    "return semi-colon as 'ARRAY_SPLIT_CHAR'" in {
      ConfigUtils.ARRAY_SPLIT_CHAR shouldBe ";"
    }
  }

  "ConfigUtils should load configuration and provide an inputToPropertyMapper method that" should {
    "give the base Schema property for a domain key" in {
      val metadataConfiguration = ConfigUtils.loadConfiguration
      val tdrFileHeaderMapper = metadataConfiguration.inputToPropertyMapper("tdrFileHeader")
      tdrFileHeaderMapper("former reference") shouldBe "former_reference_department"
      tdrFileHeaderMapper("alternate filename") shouldBe "title_alternate"
      metadataConfiguration.inputToPropertyMapper("tdrDataLoadHeader")("former_reference_department") shouldBe "former_reference_department"
      metadataConfiguration.inputToPropertyMapper("tdrDataLoadHeader")("DescriptionClosed") shouldBe "description_closed"
    }
  }

  "ConfigUtils should load configuration and provide a propertyToOutputMapper method that" should {
    "give the domain key for a given property" in {
      val metadataConfiguration = ConfigUtils.loadConfiguration
      metadataConfiguration.propertyToOutputMapper("tdrFileHeader")("former_reference_department") shouldBe "former reference"
      metadataConfiguration.propertyToOutputMapper("tdrDataLoadHeader")("date_last_modified") shouldBe "ClientSideFileLastModifiedDate"
      metadataConfiguration.propertyToOutputMapper("tdrBagitExportHeader")("file_path") shouldBe "clientside_original_filepath"
      metadataConfiguration.propertyToOutputMapper("tdrBagitExportHeader")("file_name") shouldBe "file_name"
      metadataConfiguration.propertyToOutputMapper("sharePointTag")("date_last_modified") shouldBe "Modified"
      metadataConfiguration.propertyToOutputMapper("droidHeader")("client_side_checksum") shouldBe "SHA256_HASH"
      metadataConfiguration.propertyToOutputMapper("droidHeader")("UUID") shouldBe "UUID"
      metadataConfiguration.propertyToOutputMapper("hardDriveHeader")("client_side_checksum") shouldBe "checksum"
      metadataConfiguration.propertyToOutputMapper("hardDriveHeader")("date_last_modified") shouldBe "date_last_modified"
      metadataConfiguration.propertyToOutputMapper("networkDriveHeader")("client_side_checksum") shouldBe "checksum"
      metadataConfiguration.propertyToOutputMapper("networkDriveHeader")("file_name") shouldBe "file_name"
      metadataConfiguration.propertyToOutputMapper("fclExport")("judgment_type") shouldBe "Judgment-Type"
      metadataConfiguration.propertyToOutputMapper("expectedTDRHeader")("date_last_modified") shouldBe "true"
      metadataConfiguration.propertyToOutputMapper("expectedTDRHeader")("client_side_checksum") shouldBe "false"
      metadataConfiguration.propertyToOutputMapper("allowExport")("client_side_checksum") shouldBe "false"
      metadataConfiguration.propertyToOutputMapper("allowExport")("file_path") shouldBe "true"
      metadataConfiguration.propertyToOutputMapper("blah")("blahBlah") shouldBe "blahBlah"
    }
  }

  "ConfigUtils should load configuration and provide a getPropertyType method that" should {
    "give the type config of a specified property" in {
      val metadataConfiguration = ConfigUtils.loadConfiguration
      metadataConfiguration.getPropertyType("file_path") shouldBe "string"
      metadataConfiguration.getPropertyType("end_date") shouldBe "date"
      metadataConfiguration.getPropertyType("end_dates") shouldBe ""
    }
  }

  "ConfigUtils should load configuration and provide a getMetadataProperties method that" should {
    "give the list of properties which has given property type" in {
      val metadataConfiguration = ConfigUtils.loadConfiguration
      metadataConfiguration.getPropertiesByPropertyType("System") shouldBe
        List("file_path", "file_name", "date_last_modified", "file_size", "UUID", "file_reference",
          "original_identifier", "parent_reference", "file_type", "client_side_checksum", "server_side_checksum")
      metadataConfiguration.getPropertiesByPropertyType("Supplied") shouldBe
        List("end_date", "description", "former_reference_department", "closure_type", "closure_start_date", "closure_period",
          "foi_exemption_code", "foi_exemption_asserted", "title_closed", "description_closed", "description_alternate", "title_alternate",
          "language", "file_name_translation", "rights_copyright", "restrictions_on_use", "held_by", "legal_status",
          "related_material","judgment_type", "judgment_update", "judgment_update_type", "judgment_update_details", "judgment_neutral_citation", "judgment_no_neutral_citation", "judgment_reference", "evidence_provided_by",
          "date_created", "date_range", "file_name_translation_language", "start_date", "creating_body", "file_name_language")
      metadataConfiguration.getPropertiesByPropertyType("unknown") shouldBe List()
    }
  }

  "ConfigUtils should load configuration and provide a downloadProperties method that" should {
    "give the downloadProperties config for a specified download" in {
      val metadataConfiguration = ConfigUtils.loadConfiguration
      metadataConfiguration.downloadFileDisplayProperties("MetadataDownloadTemplate").length shouldBe 21
      metadataConfiguration.downloadFileDisplayProperties("BagitExportTemplate").length shouldBe 36
      metadataConfiguration.downloadFileDisplayProperties("MetadataReviewDetailTemplate").length shouldBe 23
      metadataConfiguration.downloadFileDisplayProperties("UnknownClientTemplate").length shouldBe 0
    }

    "return the correct DownloadFilesOutput objects for a valid domain" in {
      val metadataConfiguration = ConfigUtils.loadConfiguration
      val outputs = metadataConfiguration.downloadFileDisplayProperties("MetadataDownloadTemplate")
      outputs.head.key shouldBe "file_path"
      outputs.head.columnIndex shouldBe 1
      outputs.head.editable shouldBe false
    }

    "return the correct DownloadFilesOutput for rights copyright" in {
      val metadataConfiguration = ConfigUtils.loadConfiguration
      val outputs = metadataConfiguration.downloadFileDisplayProperties("MetadataDownloadTemplate")
      val rightsCopyrightDownloadDisplay = outputs.find(property => property.key == "rights_copyright")
      rightsCopyrightDownloadDisplay match {
        case Some(property) =>
          property.key shouldBe "rights_copyright"
          property.columnIndex shouldBe 18
          property.editable shouldBe true
        case None => fail("Expected rights_copyright to be present in the download properties")
      }
    }

    "return no default for file_path" in {
      val metadataConfiguration = ConfigUtils.loadConfiguration
      val outputs = metadataConfiguration.downloadFileDisplayProperties("MetadataDownloadTemplate")
      val filePathProperty = outputs.find(property => property.key == "file_path")
      filePathProperty match {
        case Some(property) =>
          property.key shouldBe "file_path"
          property.editable shouldBe false
        case None => fail("Expected file_path to be present in the download properties")
      }
    }

    "return an empty list for an invalid domain" in {
      val metadataConfiguration = ConfigUtils.loadConfiguration
      metadataConfiguration.downloadFileDisplayProperties("InvalidDomain") shouldBe empty
    }
  }

  "ConfigUtils should load configuration and provide a getDefaultValue method that" should {
    "return the default value for a property with a default value" in {
      val metadataConfiguration = ConfigUtils.loadConfiguration
      metadataConfiguration.getDefaultValue("rights_copyright") shouldBe "Crown copyright"
    }

    "return an empty string for a property without a default value" in {
      val metadataConfiguration = ConfigUtils.loadConfiguration
      metadataConfiguration.getDefaultValue("file_path") shouldBe ""
    }

    "return an empty string for a non-existent property" in {
      val metadataConfiguration = ConfigUtils.loadConfiguration
      metadataConfiguration.getDefaultValue("non_existent_property") shouldBe ""
    }
  }

  "ConfigUtils should load configuration and provide a getPropertiesWithDefaultValue method that" should {
    "return a mapping of properties with a default value set" in {
      val expectedPropertiesWithDefaultValue = List("closure_type", "language", "legal_status", "description_closed", "title_closed", "rights_copyright", "held_by")
      val metadataConfiguration = ConfigUtils.loadConfiguration
      val mapping = metadataConfiguration.getPropertiesWithDefaultValue
      mapping.size shouldBe 7
      mapping.keys.toList shouldBe expectedPropertiesWithDefaultValue
      mapping("closure_type") shouldBe "Open"
      mapping("language") shouldBe "English"
      mapping("legal_status") shouldBe "Public Record(s)"
      mapping("description_closed") shouldBe "false"
      mapping("title_closed") shouldBe "false"
      mapping("rights_copyright") shouldBe "Crown copyright"
      mapping("held_by") shouldBe "The National Archives, Kew"
    }
  }

  "mapToEnvironmentFile" should {
    "return original filename when no ENVIRONMENT variable is set" in {
      val originalEnv = sys.props.get("ENVIRONMENT")
      sys.props.remove("ENVIRONMENT")
      
      val result = ConfigUtils.mapToEnvironmentFile("config.json")
      result shouldBe "config.json"
      
      originalEnv.foreach(sys.props("ENVIRONMENT") = _)
    }

    "return original filename for simple file without leading slash when env file does not exist" in {
      withEnvironment("dev") {
        val resourceName = "config.json"
        val result = ConfigUtils.mapToEnvironmentFile(resourceName)

        result shouldBe resourceName
      }
    }

    "return original filename for simple file with leading slash when env file does not exist" in {
      withEnvironment("dev") {
        val resourceName = "/config.json"
        val result = ConfigUtils.mapToEnvironmentFile(resourceName)

        result shouldBe resourceName
      }
    }

    "return environment-specific filename when it exists" in {
      withEnvironment("dev") {
        val result = ConfigUtils.mapToEnvironmentFile("test-config/test.json")
        result shouldBe "test-config/dev-test.json"
      }
    }

    "return original filename when environment-specific file does not exist" in {
      withEnvironment("prod") {
        val result = ConfigUtils.mapToEnvironmentFile("test-config/test.json")
        result shouldBe "test-config/test.json"
      }
    }

    "handle nested paths correctly" in {
      withEnvironment("dev") {
        val result = ConfigUtils.mapToEnvironmentFile("test-config/config/test.json")
        result shouldBe "test-config/config/dev-test.json"
      }
    }

    "preserve leading slash when environment-specific file exists" in {
      withEnvironment("dev") {
        val result = ConfigUtils.mapToEnvironmentFile("/test-config/test.json")
        result shouldBe "/test-config/dev-test.json"
      }
    }

    "preserve leading slash when environment-specific file does not exist" in {
      withEnvironment("prod") {
        val result = ConfigUtils.mapToEnvironmentFile("/test-config/test.json")
        result shouldBe "/test-config/test.json"
      }
    }

    "handle files with multiple dots in filename" in {
      withEnvironment("dev") {
        val result = ConfigUtils.mapToEnvironmentFile("/metadata-schema/baseSchema.schema.json")
        result shouldBe "/metadata-schema/baseSchema.schema.json"
      }
    }

    "transform filename by prepending environment prefix" in {
      withEnvironment("int") {
        val result = ConfigUtils.mapToEnvironmentFile("test-config/test.json")
        result shouldBe "test-config/test.json"
      }
    }

    "work with different environment values" in {
      withEnvironment("staging") {
        val result = ConfigUtils.mapToEnvironmentFile("test-config/test.json")
        result shouldBe "test-config/test.json"
      }
    }

    "handle empty paths correctly" in {
      withEnvironment("dev") {
        val result = ConfigUtils.mapToEnvironmentFile("")
        result shouldBe ""
      }
    }

    "handle single filename without directory" in {
      withEnvironment("dev") {
        val result = ConfigUtils.mapToEnvironmentFile("test.json")
        result shouldBe "test.json"
      }
    }

    "handle paths with only directory and no file" in {
      withEnvironment("dev") {
        val result = ConfigUtils.mapToEnvironmentFile("test-config/")
        result shouldBe "test-config/"
      }
    }

    "work with CONFIG_SCHEMA constant" in {
      withEnvironment("dev") {
        val result = ConfigUtils.mapToEnvironmentFile("config-schema/config.json")
        result shouldBe "config-schema/config.json"
      }
    }

    "work with BASE_SCHEMA constant pattern" in {
      withEnvironment("dev") {
        val result = ConfigUtils.mapToEnvironmentFile("/metadata-schema/baseSchema.schema.json")
        result shouldBe "/metadata-schema/baseSchema.schema.json"
      }
    }

    "handle deeply nested paths" in {
      withEnvironment("dev") {
        val result = ConfigUtils.mapToEnvironmentFile("a/b/c/d/test.json")
        result shouldBe "a/b/c/d/test.json"
      }
    }

    "return original when environment is empty string" in {
      withEnvironment("") {
        val result = ConfigUtils.mapToEnvironmentFile("test-config/test.json")
        result shouldBe "test-config/test.json"
      }
    }

    "handle files without extension" in {
      withEnvironment("dev") {
        val result = ConfigUtils.mapToEnvironmentFile("test-config/README")
        result shouldBe "test-config/README"
      }
    }

    "handle hidden files" in {
      withEnvironment("dev") {
        val result = ConfigUtils.mapToEnvironmentFile("test-config/.hidden")
        result shouldBe "test-config/.hidden"
      }
    }

    "handle files with only extension" in {
      withEnvironment("dev") {
        val result = ConfigUtils.mapToEnvironmentFile(".gitignore")
        result shouldBe ".gitignore"
      }
    }

    "not add environment prefix twice if already present" in {
      withEnvironment("dev") {
        val result = ConfigUtils.mapToEnvironmentFile("test-config/dev-test.json")
        result shouldBe "test-config/dev-test.json"
      }
    }

    "handle paths with spaces" in {
      withEnvironment("dev") {
        val result = ConfigUtils.mapToEnvironmentFile("test config/test.json")
        result shouldBe "test config/test.json"
      }
    }

    "handle paths with special characters" in {
      withEnvironment("dev") {
        val result = ConfigUtils.mapToEnvironmentFile("test-config_v2/test.json")
        result shouldBe "test-config_v2/test.json"
      }
    }

    "be case sensitive for environment variable" in {
      withEnvironment("Dev") {
        val result = ConfigUtils.mapToEnvironmentFile("test-config/test.json")
        result shouldBe "test-config/test.json"
      }
    }

    "handle relative paths with dot notation" in {
      withEnvironment("dev") {
        val result = ConfigUtils.mapToEnvironmentFile("./test-config/test.json")
        result shouldBe "./test-config/dev-test.json"
      }
    }

    "handle relative paths with parent directory notation" in {
      withEnvironment("dev") {
        val result = ConfigUtils.mapToEnvironmentFile("../test-config/test.json")
        result shouldBe "../test-config/test.json"
      }
    }

    "handle Windows-style paths" in {
      withEnvironment("dev") {
        val result = ConfigUtils.mapToEnvironmentFile("test-config\\test.json")
        result shouldBe "test-config\\test.json"
      }
    }

    "handle files with numeric names" in {
      withEnvironment("dev") {
        val result = ConfigUtils.mapToEnvironmentFile("test-config/123.json")
        result shouldBe "test-config/123.json"
      }
    }

    "handle files with hyphens and underscores" in {
      withEnvironment("dev") {
        val result = ConfigUtils.mapToEnvironmentFile("test-config/my_test-file.json")
        result shouldBe "test-config/my_test-file.json"
      }
    }

    "work correctly with different environment names" in {
      List("dev", "prod", "int", "staging", "qa", "test").foreach { env =>
        withEnvironment(env) {
          val result = ConfigUtils.mapToEnvironmentFile("test-config/test.json")
          if (env == "dev") {
            result shouldBe "test-config/dev-test.json"
          } else {
            result shouldBe "test-config/test.json"
          }
        }
      }
    }

    "handle very long filenames" in {
      withEnvironment("dev") {
        val longName = "a" * 200 + ".json"
        val result = ConfigUtils.mapToEnvironmentFile(s"test-config/$longName")
        result shouldBe s"test-config/$longName"
      }
    }

    "handle files with UTF-8 characters" in {
      withEnvironment("dev") {
        val result = ConfigUtils.mapToEnvironmentFile("test-config/tëst.json")
        result shouldBe "test-config/tëst.json"
      }
    }

    "handle paths with consecutive slashes" in {
      withEnvironment("dev") {
        val result = ConfigUtils.mapToEnvironmentFile("test-config//test.json")
        result shouldBe "test-config/dev-test.json"
      }
    }

    "handle files with query parameters in name" in {
      withEnvironment("dev") {
        val result = ConfigUtils.mapToEnvironmentFile("test-config/test.json?v=1")
        result shouldBe "test-config/test.json?v=1"
      }
    }

    "handle files with fragment identifiers" in {
      withEnvironment("dev") {
        val result = ConfigUtils.mapToEnvironmentFile("test-config/test.json#section")
        result shouldBe "test-config/test.json#section"
      }
    }

    "check sys.props before sys.env" in {
      val originalEnv = sys.props.get("ENVIRONMENT")
      sys.props("ENVIRONMENT") = "dev"
      
      val result = ConfigUtils.mapToEnvironmentFile("test-config/test.json")
      result shouldBe "test-config/dev-test.json"
      
      originalEnv match {
        case Some(value) => sys.props("ENVIRONMENT") = value
        case None => sys.props.remove("ENVIRONMENT")
      }
    }
  }
}
