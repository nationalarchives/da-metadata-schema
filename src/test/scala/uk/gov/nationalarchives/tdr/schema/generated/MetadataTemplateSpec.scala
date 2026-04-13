package uk.gov.nationalarchives.tdr.schema.generated

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

class MetadataTemplateSpec extends AnyWordSpec {

  "MetadataTemplate.all" should {
    "return a non-empty sequence of metadata properties" in {
      MetadataTemplate.all should not be empty
      MetadataTemplate.all.size should be > 15
    }

    "contain core mandatory properties" in {
      val properties = MetadataTemplate.all.map(_.property)
      properties should contain("file_path")
      properties should contain("file_name")
      properties should contain("date_last_modified")
      properties should contain("closure_type")
    }

    "contain closure-related properties" in {
      val properties = MetadataTemplate.all.map(_.property)
      properties should contain("closure_start_date")
      properties should contain("closure_period")
      properties should contain("foi_exemption_code")
      properties should contain("foi_exemption_asserted")
      properties should contain("title_closed")
      properties should contain("title_alternate")
      properties should contain("description_closed")
      properties should contain("description_alternate")
    }

    "contain language properties" in {
      val properties = MetadataTemplate.all.map(_.property)
      properties should contain("language")
      properties should contain("file_name_translation")
    }

    "contain copyright and rights properties" in {
      val properties = MetadataTemplate.all.map(_.property)
      properties should contain("rights_copyright")
      properties should contain("copyright_details")
      properties should contain("restrictions_on_use")
    }

    "contain optional metadata properties" in {
      val properties = MetadataTemplate.all.map(_.property)
      properties should contain("end_date")
      properties should contain("description")
      properties should contain("former_reference_department")
      properties should contain("related_material")
      properties should contain("evidence_provided_by")
      properties should contain("note")
    }

    "not contain duplicate properties" in {
      val properties = MetadataTemplate.all.map(_.property)
      properties.size shouldBe properties.distinct.size
    }
  }

  "MetadataTemplate property fields" should {
    "have all required fields populated" in {
      MetadataTemplate.all.foreach { prop =>
        prop.property should not be empty
        prop.details should not be null
        prop.format should not be null
        prop.tdrRequirement should not be empty
        prop.example should not be null
      }
    }

    "have valid tdrRequirement values" in {
      val validRequirements = Set("Mandatory", "Optional", "Recommended", "Mandatory for closed record", "Mandatory if filename is closed", "Mandatory if description is closed")
      MetadataTemplate.all.foreach { prop =>
        validRequirements should contain(prop.tdrRequirement)
      }
    }
  }

  "MetadataTemplate specific properties" should {

    "have language property with HTML links in example" in {
      val languageProp = MetadataTemplate.all.find(_.property == "language")
      languageProp shouldBe defined
      languageProp.get.example should include("English;Welsh;Hindi")
      languageProp.get.example should include("<a href=")
      languageProp.get.example should include("</a>")
      languageProp.get.example should include("https://tdr.nationalarchives.gov.uk/faq")
    }

    "have restrictions_on_use with correct capitalization" in {
      val restrictionsProp = MetadataTemplate.all.find(_.property == "restrictions_on_use")
      restrictionsProp shouldBe defined
      restrictionsProp.get.details should include("Digital Transfer Adviser")
      restrictionsProp.get.details should not include "digital transfer advisor"
      restrictionsProp.get.tdrRequirement shouldBe "Optional"
    }

    "have file_path marked as Do not modify" in {
      val filePathProp = MetadataTemplate.all.find(_.property == "file_path")
      filePathProp shouldBe defined
      filePathProp.get.format shouldBe "Do not modify"
      filePathProp.get.details should include("do not modify")
    }

    "have file_name marked as Do not modify" in {
      val fileNameProp = MetadataTemplate.all.find(_.property == "file_name")
      fileNameProp shouldBe defined
      fileNameProp.get.format shouldBe "Do not modify"
    }

    "have date_last_modified marked as Do not modify" in {
      val dateProp = MetadataTemplate.all.find(_.property == "date_last_modified")
      dateProp shouldBe defined
      dateProp.get.format shouldBe "Do not modify"
    }
  }

  "MetadataTemplate HTML content" should {
    "have properly formed links where present" in {
      val propsWithLinks = MetadataTemplate.all.filter(p => p.example.contains("<a href") || p.details.contains("<a href") || p.format.contains("<a href"))
      propsWithLinks should not be empty
      propsWithLinks.foreach { prop =>
        if (prop.example.contains("<a href")) {
          prop.example should include("</a>")
          prop.example should include("https://")
        }
        if (prop.details.contains("<a href")) {
          prop.details should include("</a>")
          prop.details should include("https://")
        }
        if (prop.format.contains("<a href")) {
          prop.format should include("</a>")
          prop.format should include("https://")
        }
      }
    }
  }

  "MetadataTemplate.MetadataProperty case class" should {
    "have correct field types" in {
      val prop = MetadataTemplate.MetadataProperty("test", "details", "format", "Mandatory", "example")
      prop.property shouldBe a[String]
      prop.details shouldBe a[String]
      prop.format shouldBe a[String]
      prop.tdrRequirement shouldBe a[String]
      prop.example shouldBe a[String]
    }

    "allow creating properties programmatically" in {
      val prop = MetadataTemplate.MetadataProperty(
        property = "test_property",
        details = "Test details",
        format = "Text",
        tdrRequirement = "Optional",
        example = "test example"
      )
      prop.property shouldBe "test_property"
      prop.details shouldBe "Test details"
      prop.format shouldBe "Text"
      prop.tdrRequirement shouldBe "Optional"
      prop.example shouldBe "test example"
    }
  }

  "MetadataTemplate filtering and querying" should {
    "allow finding properties by requirement type" in {
      val mandatoryProps = MetadataTemplate.all.filter(_.tdrRequirement == "Mandatory")
      mandatoryProps should not be empty
      mandatoryProps.map(_.property) should contain("file_path")
      mandatoryProps.map(_.property) should contain("file_name")
    }

    "allow finding optional properties" in {
      val optionalProps = MetadataTemplate.all.filter(_.tdrRequirement == "Optional")
      optionalProps should not be empty
      optionalProps.map(_.property) should contain("end_date")
    }

    "allow finding closure-related mandatory properties" in {
      val closureMandatory = MetadataTemplate.all.filter(_.tdrRequirement == "Mandatory for closed record")
      closureMandatory should not be empty
      closureMandatory.map(_.property) should contain("foi_exemption_code")
      closureMandatory.map(_.property) should contain("closure_start_date")
    }

    "allow finding properties by name" in {
      val languageProp = MetadataTemplate.all.find(_.property == "language")
      languageProp shouldBe defined
      languageProp.get.property shouldBe "language"
    }
  }
}

