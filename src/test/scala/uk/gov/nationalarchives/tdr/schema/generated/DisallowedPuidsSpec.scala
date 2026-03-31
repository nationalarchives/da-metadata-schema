package uk.gov.nationalarchives.tdr.schema.generated

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

class DisallowedPuidsSpec extends AnyWordSpec {

  "DisallowedPuids.all" should {
    "return a non-empty sequence of PUID entries" in {
      DisallowedPuids.all should not be empty
      DisallowedPuids.all.size should be > 10
    }

    "contain the empty string PUID for unidentified files" in {
      val unidentified = DisallowedPuids.all.find(_.puid == "")
      unidentified shouldBe defined
      unidentified.get.reason shouldBe "Unidentified"
      unidentified.get.puidDescription shouldBe "Unidentified file"
      unidentified.get.active shouldBe true
    }

    "contain Zip format PUIDs" in {
      val zipPuids = DisallowedPuids.all.filter(_.reason == "Zip")
      zipPuids should not be empty
      zipPuids.size should be > 5
    }

    "contain specific known disallowed PUIDs" in {
      val puids = DisallowedPuids.all.map(_.puid)
      puids should contain("fmt/1070") // Preferred Executable Format
      puids should contain("fmt/1071") // Apple Disk Image
      puids should contain("fmt/111")  // OLE2 Compound Document Format
    }

    "have all entries with non-empty reasons" in {
      DisallowedPuids.all.foreach { entry =>
        entry.reason should not be empty
      }
    }

    "have all entries with non-empty descriptions" in {
      DisallowedPuids.all.foreach { entry =>
        entry.puidDescription should not be empty
      }
    }

    "contain only boolean values for active field" in {
      DisallowedPuids.all.foreach { entry =>
        entry.active shouldBe a[java.lang.Boolean]
      }
    }
  }

  "DisallowedPuids.PuidEntry case class" should {
    "have correct field types" in {
      val entry = DisallowedPuids.PuidEntry("test", active=true, "reason", "description")
      entry.puid shouldBe a[String]
      entry.active shouldBe a[java.lang.Boolean]
      entry.reason shouldBe a[String]
      entry.puidDescription shouldBe a[String]
    }

    "allow creating entries programmatically" in {
      val entry = DisallowedPuids.PuidEntry("fmt/9999", active=false, "Test", "Test Description")
      entry.puid shouldBe "fmt/9999"
      entry.active shouldBe false
      entry.reason shouldBe "Test"
      entry.puidDescription shouldBe "Test Description"
    }
  }

  "DisallowedPuids filtering" should {
    "allow finding PUIDs by reason" in {
      val unidentifiedPuids = DisallowedPuids.all.filter(_.reason == "Unidentified")
      unidentifiedPuids should not be empty
    }

    "allow finding active PUIDs" in {
      val activePuids = DisallowedPuids.all.filter(_.active)
      activePuids should not be empty
    }

    "allow finding specific PUID by identifier" in {
      val specificPuid = DisallowedPuids.all.find(_.puid == "fmt/1070")
      specificPuid shouldBe defined
      specificPuid.get.puidDescription shouldBe "Preferred Executable Format"
    }
  }

  "DisallowedPuids data integrity" should {
    "not contain duplicate PUIDs" in {
      val puids = DisallowedPuids.all.map(_.puid)
      puids.size shouldBe puids.distinct.size
    }

    "have consistent data structure across all entries" in {
      DisallowedPuids.all.foreach { entry =>
        entry.puid should not be null
        entry.reason should not be null
        entry.puidDescription should not be null
      }
    }
  }
}

