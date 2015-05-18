import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{FunSpec, Matchers}

class TweeterSpec extends FunSpec with Matchers with ScalaFutures {
  describe("searching for jenny holzer mentions") {
    it("returns a future with tweets in it") {
      whenReady(Tweeter().searchForJenny, timeout(Span(5, Seconds))) { s =>
        s.statuses should not be empty
      }
    }
  }
}
