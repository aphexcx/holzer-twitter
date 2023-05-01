//TRUISMS (1978-1983)
//JENNY HOLZER
// twitter bot using a scheduled actor to tweet out jenny holzer's truisms
// aphex.cx
// based on repatch-twitter
// http://eed3si9n.com/howto-write-a-dispatch-plugin

import akka.actor.ActorSystem

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.io.Source
import scala.language.postfixOps
import scala.util.{Failure, Success}

object Main extends App {
  val system = ActorSystem("MySystem")
  val tweeter = Tweeter()

  def quotes: Seq[String] = Source.fromFile(R("truisms.txt")).getLines().toSeq map (_.trim)

  def R(s: String): String = getClass.getResource(s).getPath

  def tweetAQuote(): Unit = {
    // tweet out a truism.
    // if successful, mark the quote as used and print the tweet.
    // if failed, print the exception.
    val recorder = Recorder("used.bin")
    tweeter.postTweetAndMarkUsed(recorder.getAnUnusedQuote(quotes), recorder)
  }

  def searchAndRespond(): Unit =
    tweeter.searchForJenny onComplete {
      case Success(tweets) => tweeter.respondToTweets(tweets)
      case Failure(e) => println("Error when searching: " + e.getMessage)
    }


  system.scheduler.schedule(0 seconds, 10.5 hours) {
    tweetAQuote()
  }

  system.scheduler.schedule(0 seconds, 120 seconds) {
    searchAndRespond()
  }

  // future: if i want to tweet at specific times of day
  // https://github.com/theatrus/akka-quartz
  //  val quartzActor = system.actorOf(Props[QuartzActor])
}
