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

  "config.json" should {
    val nodeSchema = Using(Source.fromResource("config-schema/config.json"))(_.mkString)
    val mapper = new ObjectMapper()
    val configData = mapper.readTree(nodeSchema.get).toPrettyString
    val propertyKeys = decode[Config](configData)
      .getOrElse(Config(List.empty[ConfigItem])).configItems.map(_.key)

    "contain the correct number of properties" in {
      propertyKeys.size should equal(38)
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
          "related_material","judgment_type", "judgment_update", "judgment_update_type", "judgment_update_details", "judgment_neutral_citation", "judgment_no_neutral_citation", "judgment_reference", "evidence_provided_by")
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
}
