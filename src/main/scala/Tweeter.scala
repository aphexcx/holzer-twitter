import Main.quotes
import com.danielasfregola.twitter4s.entities.{AccessToken, ConsumerToken, Tweet, User}
import com.danielasfregola.twitter4s.{TwitterRestClient, entities}
import myImplicits.wrapAny

import java.io.File
import java.util.Properties
import scala.concurrent.Future
import scala.io.Source
import scala.util.{Failure, Success}

class Tweeter {

  import scala.concurrent.ExecutionContext.Implicits.global

  private val propFile = new File("auth.properties")

  private val properties = new Properties()
  properties.load(Source.fromFile(propFile).bufferedReader())

  private val consumerKey: String = properties.getProperty("twitter.consumerKey")
  private val consumerSecret: String = properties.getProperty("twitter.consumerKeySecret")
  private val accessKey: String = properties.getProperty("twitter.accessToken")
  private val accessSecret: String = properties.getProperty("twitter.accessTokenSecret")

  private val consumerToken: ConsumerToken = ConsumerToken(key = consumerKey, secret = consumerSecret)
  private val accessToken: AccessToken = AccessToken(key = accessKey, secret = accessSecret)

  private val client = new TwitterRestClient(consumerToken, accessToken)

  def postTweetAndMarkUsed(s: String, recorder: Recorder): Unit = {
    postTweet(s) onComplete {
      case Success(t) => recorder.markUsed(s); println(t)
      case Failure(e) => println("Error posting tweet: " + e.getMessage)
    }
  }

  private def postTweet(q: String): Future[Tweet] = {
    client.createTweet(q)
  }

  def respondToTweets(tweets: List[Tweet]): Unit =
    tweets foreach respondToTweet

  private def respondToTweet(t: Tweet): Unit = {
    val respondedTweets = Recorder("respondedTweetIDs.bin")

    if (!respondedTweets.used.contains(t.id.toString)) {
      t.user foreach { user =>
        val q = Recorder("used.bin").getAnUnusedQuote(quotes)
        postReply(q, user.screen_name, t.id) onComplete {
          case Success(postedTweet) => respondedTweets.markUsed(t.id.toString); println(postedTweet)
          case Failure(e) => println("Error posting tweet: " + e.getMessage)

        }
      }
    }
  }

  def postReply(r: String, inReplyToScreenName: String, inReplyToTweetID: Long): Future[Tweet] = {
    client.createTweet("@%s %s".format(inReplyToScreenName, r), Option(inReplyToTweetID))
  }

  def searchForJenny: Future[List[entities.Tweet]] =
    client.searchTweet("jenny holzer", count = 100)
      .map(_.data.statuses) tap { _ => println("Searching!") }

  def destroyStatus(id: Long): Future[Tweet] = {
    client.deleteTweet(id)
  }

  def getUser(screen_name: String): Future[User] = {
    client.user(screen_name).map(_.data)
  }

}

object Tweeter {

  def apply(): Tweeter = {
    new Tweeter
  }
}
