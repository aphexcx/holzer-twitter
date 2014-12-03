import java.io.File
import java.nio.file.NoSuchFileException

import Main._
import org.scalatest.{FunSpec, Matchers}

class MainSpec extends FunSpec with Matchers {
  describe("use") {
    describe("given a string and a set") {
      it("returns a new set containing the string and the previous set") {
        val s = "hello"
        val set = Set[String]("goodbye", "farewell")
        val set2 = Set[String]("hello", "goodbye", "farewell")
        use(s, set) shouldBe set2
      }
    }
  }

  describe("dump and load") {
    it("writes a given set to a file and reads it back") {
      val set = Set[String]("hi", "bye")
      val path = File.createTempFile("mainspec", "dump").getPath
      dump(set, path)
      load(path) shouldBe set
    }

    it("throws an exception if the file to load from doesn't exist") {
      an[NoSuchFileException] shouldBe thrownBy(load("bogus"))
    }
  }

}
