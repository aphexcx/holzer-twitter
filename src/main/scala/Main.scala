//TRUISMS (1978-1983)
//JENNY HOLZER
// twitter bot using a scheduled actor to tweet out jenny holzer's truisms
// aphex.cx
// based on repatch-twitter
// http://eed3si9n.com/howto-write-a-dispatch-plugin

import java.io.File
import java.nio.file.{Files, Paths}

import akka.actor.ActorSystem
import dispatch.{Http, _}
import twitter.response
import twitter.response.{Tweet, User}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.io.Source
import scala.language.postfixOps
import scala.pickling._
import scala.pickling.binary._
import scala.util.{Failure, Random, Success}

object Main extends App {
  val prop = new File("auth.properties")
  val client: OAuthClient = PropertiesClient(prop)
  val http = new Http
  val system = ActorSystem("MySystem")

  def quotes: Seq[String] = Source.fromFile(R("truisms.txt")).getLines().toSeq map (_.trim)

  def R(s: String): String = getClass.getResource(s).getPath

  case class Recorder(filename: String) {

    if (!Files.exists(Paths.get(filename)))
      Files.createFile(Paths.get(filename))

    def dump(xs: Set[String]): Unit = {
      Files.write(Paths.get(filename), xs.pickle.value)
    }

    def load: Set[String] = {
      val ab: Array[Byte] = Files.readAllBytes(Paths.get(filename))
      if (ab.length == 0)
        Set.empty[String]
      else
        BinaryPickle(ab).unpickle[Set[String]]
    }

    def use(s: String, used: Set[String]): Set[String] =
      used + s.trim

    def used: Set[String] = load

    def markUsed(s: String) = dump(use(s, used))

    def reset() = dump(Set.empty[String])

  }

  def unusedQuote(quotes: Seq[String], used: Set[String]): Option[String] =
    Random.shuffle(quotes filterNot used).headOption


  def getAnUnusedQuote(quotes: Seq[String], recorder: Recorder): String =
    unusedQuote(quotes, recorder.used).getOrElse {
      recorder.reset()
      unusedQuote(quotes, recorder.used).get
    }

  //  def postWithRollover(quotes: Seq[String], recorder: Recorder,
  //                       effect: (String, Recorder) => Unit): Unit =
  //    getAnUnusedQuote(quotes, recorder)
  //    unusedQuote(quotes, recorder.used).map(q => effect(q, recorder)) orElse {
  ////      val fresh = Set.empty[String]
  //      recorder.reset()
  //      unusedQuote(quotes, recorder.used).map(q => effect(q, recorder))
  //    }

  // need to post tweet and record the tweet used.
  //  def postTweet(q: String)(onSuccess: (Set[String], String) => Unit): Unit = {
  //    val f: Future[Tweet] = http(client(Status.update(q)) OK as.repatch.twitter.response.Tweet)
  //    f onComplete {
  //      case Success(t) => dump(use(q, used)); println(t)
  //      case Failure(e) => println("Error posting tweet: " + e.getMessage)
  //    }
  //  }

  def postTweet(q: String): Future[Tweet] = {
    http(client(Status.update(q)) OK as.repatch.twitter.response.Tweet)
  }

  def postTweetAndMarkUsed(s: String, recorder: Recorder): Unit = {
    postTweet(s) onComplete {
      case Success(t) => recorder.markUsed(s); println(t)
      case Failure(e) => println("Error posting tweet: " + e.getMessage)
    }
  }

  def tweetAQuote(): Unit = {
    // tweet out a truism.
    // if successful, mark the quote as used and print the tweet.
    // if failed, print the exception.
    val recorder = Recorder("used.bin")
    postTweetAndMarkUsed(getAnUnusedQuote(quotes, recorder), recorder)
  }

  //    postTweet(unusedQuote(quotes),) onComplete {
  //      case Success(t) => Recorder("used.bin").markUsed(s); println(t)
  //      case Failure(e) => println("Error posting tweet: " + e.getMessage)
  //    }
  //, dump(filename = "used.bin"), postTweetAndMarkUsed)


  // future: if i want to tweet at specific times of day
  // https://github.com/theatrus/akka-quartz
  //  val quartzActor = system.actorOf(Props[QuartzActor])

  def respondToTweets(tweets: List[Tweet]): Unit =
    tweets foreach respondToTweet


  def respondToTweet(t: Tweet): Unit = {
    val respondedTweets = Recorder("respondedTweetIDs.bin")

    if (!respondedTweets.used.contains(t.id.toString())) {
      t.user foreach { user =>
        val q = getAnUnusedQuote(quotes, Recorder("used.bin"))
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


  def searchAndRespond(): Unit =
    searchForJenny onComplete {
      case Success(s) => respondToTweets(s.statuses)
      case Failure(e) => println("Error when searching: " + e.getMessage)
    }


  system.scheduler.schedule(0 seconds, 10.5 hours) {
    tweetAQuote()
  }

  system.scheduler.schedule(0 seconds, 120 seconds) {
    searchAndRespond()
  }
}
