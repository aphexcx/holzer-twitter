import java.io.File

import Main._
import org.scalatest.{FunSpec, Matchers}


class MainSpec extends FunSpec with Matchers {
  describe("Recorder") {
    val path = File.createTempFile("mainspec", "dump").getPath
    val recorder = Recorder(path)
    describe("use") {
      describe("given a string and a set") {
        it("returns a new set containing the string and the previous set") {
          val s = "hello"
          val set = Set[String]("goodbye", "farewell")
          val set2 = Set[String]("hello", "goodbye", "farewell")
          recorder.use(s, set) shouldBe set2
        }
      }
    }

    describe("dump and load") {
      it("writes a given set to a file and reads it back") {
        val set = Set[String]("hi", "bye")

        recorder.dump(set)
        recorder.load shouldBe set
      }

      //      it("throws an exception if the file to load from doesn't exist") {
      //        an[NoSuchFileException] shouldBe thrownBy(Recorder("bogus"))
      //      }
    }
  }

  describe("unused quote rollover") {
    val path = File.createTempFile("mainspec", "dump").getPath
    val recorder = Recorder(path)

    val quotes: Seq[String] = Seq("a", "b", "c")

    it("returns an unused quote when there's one available") {
      getAnUnusedQuote(quotes, recorder) should (be("a") or be("b") or be("c"))
    }

    it("rolls over correctly when all the quotes are used up") {
      recorder.markUsed("a")
      getAnUnusedQuote(quotes, recorder) should (be("b") or be("c"))
      recorder.markUsed("b")
      getAnUnusedQuote(quotes, recorder) should be("c")
      recorder.markUsed("c")
      getAnUnusedQuote(quotes, recorder) should (be("a") or be("b") or be("c"))

    }
  }

}
