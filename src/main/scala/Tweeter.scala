import java.io.File

import Main.quotes
import dispatch._
import twitter.response
import twitter.response.Tweet

import scala.util.{Failure, Success}

class Tweeter {
  import scala.concurrent.ExecutionContext.Implicits.global

  val prop = new File("auth.properties")
  val client: OAuthClient = PropertiesClient(prop)
  val http = new Http

  def postTweet(q: String): Future[Tweet] = {
    http(client(Status.update(q)) OK as.repatch.twitter.response.Tweet)
  }

  def postReply(r: String, inReplyToScreenName: String, inReplyToTweetID: BigInt): Future[Tweet] = {
    //TODO:remove get
    http(client(Status.reply(r, inReplyToScreenName, inReplyToTweetID)) OK as.repatch.twitter.response.Tweet)
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
        postReply(q, user.screen_name, t.id) onComplete {
          case Success(postedTweet) => respondedTweets.markUsed(t.id.toString()); println(postedTweet)
          case Failure(e) => println("Error posting tweet: " + e.getMessage)

        }
      }
    }
  }

  def searchForJenny: Future[response.Search] =
    http(client(Search("jenny holzer")) OK as.repatch.twitter.response.Search)

  def destroyStatus(id: BigInt): Future[Tweet] =
    http(client(Status.DestroyStatus(id)) OK as.repatch.twitter.response.Status)

  def getUser(screen_name: String) =
    http(client(User.show(screen_name)) OK as.repatch.twitter.response.User)



}

object Tweeter {

  def apply(): Tweeter = {
    new Tweeter
  }
}
