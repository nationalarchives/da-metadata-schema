package uk.gov.nationalarchives.tdr.schemautils

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

class ConfigUtilsSpec extends AnyWordSpec {

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
      metadataConfiguration.getPropertiesByPropertyType("System") shouldBe List("file_path", "file_name", "date_last_modified", "client_side_checksum", "file_size", "UUID", "file_reference", "original_identifier", "parent_reference", "file_type", "client_side_checksum")
      metadataConfiguration.getPropertiesByPropertyType("unknown") shouldBe List()
    }
  }

  "ConfigUtils should load configuration and provide a downloadProperties method that" should {
    "give the downloadProperties config for a specified download" in {
      val metadataConfiguration = ConfigUtils.loadConfiguration
      metadataConfiguration.downloadFileDisplayProperties("MetadataDownloadTemplate").length shouldBe 19
      metadataConfiguration.downloadFileDisplayProperties("BagitExportTemplate").length shouldBe 27
      metadataConfiguration.downloadFileDisplayProperties("MetadataReviewDetailTemplate").length shouldBe 20
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

}
