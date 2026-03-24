package uk.gov.nationalarchives.tdr.schemautils

import io.circe.generic.auto._
import io.circe.jawn.decode
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

import scala.io.Source
import scala.util.Using

class PUIDChecksumSpec extends AnyWordSpec {

  case class ChecksumEntry(checksum: String, checksumDescription: String)
  case class PUIDChecksum(puid: String, checksums: List[ChecksumEntry])

  "puid-checksums.json" should {
    val json = Using(Source.fromResource("puids/puid-checksums.json"))(_.mkString).get
    val puidChecksums = decode[List[PUIDChecksum]](json).getOrElse(List.empty)

    "contain a single zeroByteFile entry" in {
      puidChecksums.size should equal(1)
      puidChecksums.head.puid should equal("zeroByteFile")
    }

    "contain two checksums for zeroByteFile" in {
      val checksums = puidChecksums.head.checksums
      checksums.size should equal(2)
    }

    "contain the zero-byte file checksum" in {
      val checksums = puidChecksums.head.checksums
      checksums.map(_.checksum) should contain("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
    }

    "contain the BOM file checksum" in {
      val checksums = puidChecksums.head.checksums
      checksums.map(_.checksum) should contain("f01a374e9c81e3db89b3a42940c4d6a5447684986a1296e42bf13f196eed6295")
    }
  }
}

