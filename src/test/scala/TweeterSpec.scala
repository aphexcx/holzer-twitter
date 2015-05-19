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

  describe("responding to tweets") {
    it("returns a future with the postedTweet") {
      val tw = Tweeter()
      whenReady(tw.getUser("HOLZERTRON"), timeout(Span(5, Seconds))) { u =>
        whenReady(tw.postReply("test", u.screen_name, u.status.get.id), timeout(Span(5, Seconds))) { t =>
          t.text shouldBe "@HOLZERTRON test"
          t.in_reply_to_user_id shouldBe Some(u.id)

          // clean up
          whenReady(tw.destroyStatus(t.id), timeout(Span(5, Seconds))) { destroyed =>
            destroyed.text shouldBe "@HOLZERTRON test"
          }
        }
      }
    }
  }
}
