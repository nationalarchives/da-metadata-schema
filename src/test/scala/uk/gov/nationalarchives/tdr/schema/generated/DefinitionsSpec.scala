package uk.gov.nationalarchives.tdr.schema.generated

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

class DefinitionsSpec extends AnyWordSpec {

  "Definitions.foi_codes" should {
    "contain standard FOI exemption codes" in {
      val foiCodes = Definitions.foi_codes.all
      foiCodes should contain("23")
      foiCodes should contain("24")
      foiCodes should contain("27(1)")
      foiCodes should contain("27(2)")
      foiCodes should contain("33")
      foiCodes should contain("40(2)")
    }

    "contain EIR exemption codes" in {
      val foiCodes = Definitions.foi_codes.all
      foiCodes should contain("EIR 12(5)(a)")
      foiCodes should contain("EIR 12(5)(b)")
      foiCodes should contain("EIR 12(5)(c)")
    }

    "contain section 35 subsection codes" in {
      val foiCodes = Definitions.foi_codes.all
      foiCodes should contain("35(1)(a)")
      foiCodes should contain("35(1)(b)")
      foiCodes should contain("35(1)(c)")
      foiCodes should contain("35(1)(d)")
    }

    "contain section 37 subsection codes" in {
      val foiCodes = Definitions.foi_codes.all
      foiCodes should contain("37(1)(a)")
      foiCodes should contain("37(1)(aa)")
      foiCodes should contain("37(1)(ab)")
      foiCodes should contain("37(1)(ac)")
      foiCodes should contain("37(1)(ad)")
      foiCodes should contain("37(1)(b)")
    }

    "have at least 39 codes" in {
      Definitions.foi_codes.all.size should be >= 39
    }

    "not contain duplicate codes" in {
      val foiCodes = Definitions.foi_codes.all
      foiCodes.size shouldBe foiCodes.distinct.size
    }
  }

  "Definitions.languages" should {
    "contain English and Welsh" in {
      val languages = Definitions.languages.all
      languages should contain("English")
      languages should contain("Welsh")
    }

    "contain Asian languages" in {
      val languages = Definitions.languages.all
      languages should contain("Hindi")
      languages should contain("Bengali")
      languages should contain("Chinese")
      languages should contain("Punjabi")
      languages should contain("Urdu")
      languages should contain("Gujarati")
    }

    "contain European languages" in {
      val languages = Definitions.languages.all
      languages should contain("German")
      languages should contain("Italian")
      languages should contain("Polish")
      languages should contain("Portuguese")
      languages should contain("Russian")
      languages should contain("Latvian")
      languages should contain("Lithuanian")
    }

    "contain Gaelic languages" in {
      val languages = Definitions.languages.all
      languages should contain("Irish Gaelic")
      languages should contain("Scottish Gaelic")
      languages should contain("Ulster Scots")
    }

    "contain other languages" in {
      val languages = Definitions.languages.all
      languages should contain("Arabic")
      languages should contain("Somali")
      languages should contain("Vietnamese")
    }

    "have exactly 21 languages" in {
      Definitions.languages.all.size shouldBe 21
    }

    "not contain duplicate languages" in {
      val languages = Definitions.languages.all
      languages.size shouldBe languages.distinct.size
    }
  }

  "Definitions.closure_types" should {
    "contain Open and Closed only" in {
      Definitions.closure_types.all should contain theSameElementsAs Seq("Closed", "Open")
    }

    "have exactly 2 closure types" in {
      Definitions.closure_types.all.size shouldBe 2
    }
  }

  "Definitions.file_types" should {
    "contain File and Folder only" in {
      Definitions.file_types.all should contain theSameElementsAs Seq("File", "Folder")
    }

    "have exactly 2 file types" in {
      Definitions.file_types.all.size shouldBe 2
    }
  }

  "Definitions.legal_statuses" should {
    "contain all legal status options" in {
      val legalStatuses = Definitions.legal_statuses.all
      legalStatuses should contain("Public Record(s)")
      legalStatuses should contain("Not Public Record(s)")
      legalStatuses should contain("Welsh Public Record(s)")
    }

    "have exactly 3 legal statuses" in {
      Definitions.legal_statuses.all.size shouldBe 3
    }
  }

  "Definitions.held_by" should {
    "contain The National Archives location" in {
      Definitions.held_by.all should contain("The National Archives, Kew")
    }

    "have at least 1 location" in {
      Definitions.held_by.all.size should be >= 1
    }
  }

  "Definitions.copyrights" should {
    "contain Crown copyright options" in {
      val copyrights = Definitions.copyrights.all
      copyrights should contain("Crown copyright")
      copyrights should contain("Crown")
    }

    "contain Third party option" in {
      Definitions.copyrights.all should contain("Third party")
    }

    "contain Parliament options" in {
      val copyrights = Definitions.copyrights.all
      copyrights should contain("UK Parliament")
      copyrights should contain("Open Parliament Licence")
    }

    "contain Unknown option" in {
      Definitions.copyrights.all should contain("Unknown")
    }

    "have at least 5 copyright types" in {
      Definitions.copyrights.all.size should be >= 5
    }
  }
}

