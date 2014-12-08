//TRUISMS (1978-1983)
//JENNY HOLZER
// twitter bot using a scheduled actor to tweet out jenny holzer's truisms
// aphex.cx
// based on repatch-twitter
// http://eed3si9n.com/howto-write-a-dispatch-plugin

import java.io.File
import java.nio.file.{Files, Paths}

import akka.actor.ActorSystem
import dispatch._
import twitter.response.Tweet

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.io.Source
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

  if (!Files.exists(Paths.get("used.bin")))
    Files.createFile(Paths.get("used.bin"))

  def load(filename: String): Set[String] = {
    val ab: Array[Byte] = Files.readAllBytes(Paths.get(filename))
    if (ab.length == 0)
      Set.empty[String]
    else
      BinaryPickle(ab).unpickle[Set[String]]
  }

  def quote(lines: Seq[String], used: Set[String]): String =
    Random.shuffle(lines filterNot used) head

  def dump(xs: Set[String], filename: String = "used.bin"): Unit = {
    Files.write(Paths.get(filename), xs.pickle.value)
  }

  def use(s: String, used: Set[String]): Set[String] =
    used + s.trim

  def tweetAQuote(): Unit = {
    // tweet out a truism.
    // if successful, mark the quote as used and print the tweet.
    // if failed, print the exception.
    val used: Set[String] = load("used.bin")
    val q: String = quote(quotes, used)
    val f: Future[Tweet] = http(client(Status.update(q)) OK as.repatch.twitter.response.Tweet)
    f onComplete {
      case Success(t) => dump(use(q, used)); println(t)
      case Failure(e) => println("Error posting tweet: " + e.getMessage)
    }
  }

  // future: if i want to tweet at specific times of day
  // https://github.com/theatrus/akka-quartz
  //  val quartzActor = system.actorOf(Props[QuartzActor])

  system.scheduler.schedule(0 seconds, 10.5 hours) {
    tweetAQuote()
  }
}
