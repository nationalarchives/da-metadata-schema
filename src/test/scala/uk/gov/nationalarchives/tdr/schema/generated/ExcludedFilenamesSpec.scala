package uk.gov.nationalarchives.tdr.schema.generated

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ExcludedFilenamesSpec extends AnyFlatSpec with Matchers {

  "ExcludedFilenames.isExcluded" should "exclude thumbs.db (case insensitive)" in {
    ExcludedFilenames.isExcluded("thumbs.db") shouldBe true
    ExcludedFilenames.isExcluded("Thumbs.db") shouldBe true
    ExcludedFilenames.isExcluded("THUMBS.DB") shouldBe true
    ExcludedFilenames.isExcluded("ThUmBs.Db") shouldBe true
  }

  it should "exclude desktop.ini (case insensitive)" in {
    ExcludedFilenames.isExcluded("desktop.ini") shouldBe true
    ExcludedFilenames.isExcluded("Desktop.ini") shouldBe true
    ExcludedFilenames.isExcluded("DESKTOP.INI") shouldBe true
    ExcludedFilenames.isExcluded("DeskTop.Ini") shouldBe true
  }

  it should "exclude .DS_Store (case sensitive)" in {
    ExcludedFilenames.isExcluded(".DS_Store") shouldBe true
    ExcludedFilenames.isExcluded(".ds_store") shouldBe false // case sensitive
    ExcludedFilenames.isExcluded(".DS_STORE") shouldBe false // case sensitive
  }

  it should "not exclude regular files" in {
    ExcludedFilenames.isExcluded("document.pdf") shouldBe false
    ExcludedFilenames.isExcluded("image.jpg") shouldBe false
    ExcludedFilenames.isExcluded("spreadsheet.xlsx") shouldBe false
  }

  it should "not exclude files with similar names" in {
    ExcludedFilenames.isExcluded("my_thumbs.db") shouldBe false // doesn't match exact pattern
    ExcludedFilenames.isExcluded("desktop_settings.ini") shouldBe false
  }

  "filtering with isExcluded" should "filter out excluded filenames from a list" in {
    val filenames = Seq(
      "document.pdf",
      "thumbs.db",
      "image.jpg",
      "Desktop.ini",
      ".DS_Store",
      "spreadsheet.xlsx",
      "THUMBS.DB"
    )

    val filtered = filenames.filterNot(ExcludedFilenames.isExcluded)

    filtered should contain theSameElementsAs Seq(
      "document.pdf",
      "image.jpg",
      "spreadsheet.xlsx"
    )
  }

  it should "return empty list when all files are excluded" in {
    val filenames = Seq("thumbs.db", "Desktop.ini", ".DS_Store")
    filenames.filterNot(ExcludedFilenames.isExcluded) shouldBe empty
  }

  it should "return all files when none are excluded" in {
    val filenames = Seq("document.pdf", "image.jpg", "spreadsheet.xlsx")
    filenames.filterNot(ExcludedFilenames.isExcluded) should contain theSameElementsAs filenames
  }
}
