import java.io.File

import Main.quotes
import dispatch._
import twitter.response
import twitter.response.{Tweet, User}

import scala.util.{Failure, Success}

class Tweeter {

  import scala.concurrent.ExecutionContext.Implicits.global

  val prop = new File("auth.properties")
  val client: OAuthClient = PropertiesClient(prop)
  val http = new Http

  def postTweet(q: String): Future[Tweet] = {
    http(client(Status.update(q)) OK as.repatch.twitter.response.Tweet)
  }


  def postTweetAndMarkUsed(s: String, recorder: Recorder): Unit = {
    postTweet(s) onComplete {
      case Success(t) => recorder.markUsed(s); println(t)
      case Failure(e) => println("Error posting tweet: " + e.getMessage)
    }
  }

  def respondToTweets(tweets: List[Tweet]): Unit =
    tweets foreach respondToTweet


  def respondToTweet(t: Tweet): Unit = {
    val respondedTweets = Recorder("respondedTweetIDs.bin")

    if (!respondedTweets.used.contains(t.id.toString())) {
      t.user foreach { user =>
        val q = Recorder("used.bin").getAnUnusedQuote(quotes)
        replyToUser(user, q) onComplete {
          case Success(postedTweet) => respondedTweets.markUsed(t.id.toString()); println(postedTweet)
          case Failure(e) => println("Error posting tweet: " + e.getMessage)

        }
      }
    }

    def replyToUser(user: User, s: String): Future[Tweet] =
      postTweet("@" + user + " " + s)
  }

  def searchForJenny: Future[response.Search] =
    http(client(Search("jenny holzer")) OK as.repatch.twitter.response.Search)

}

object Tweeter {

  def apply(): Tweeter = {
    new Tweeter
  }
}
