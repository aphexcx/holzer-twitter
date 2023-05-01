import io.circe.parser._
import io.circe.syntax._

import java.nio.file.{Files, Paths}
import scala.util.Random

case class Recorder(filename: String) {

  if (!Files.exists(Paths.get(filename)))
    Files.createFile(Paths.get(filename))

  def load: Set[String] = {
    val jsonString = new String(Files.readAllBytes(Paths.get(filename)))
    decode[Set[String]](jsonString) match {
      case Right(set) => set
      case Left(_) => Set.empty[String]
    }
  }

  def markUsed(s: String): Unit = dump(use(s, used))

  def use(s: String, used: Set[String]): Set[String] =
    used + s.trim

  def used: Set[String] = load

  def unused(quotes: Seq[String], used: Set[String]): Option[String] =
    Random.shuffle(quotes filterNot used).headOption

  def getAnUnusedQuote(quotes: Seq[String]): String =
    unused(quotes, used).getOrElse {
      reset()
      unused(quotes, used).get
    }

  def reset(): Unit = dump(Set.empty[String])

  def dump(xs: Set[String]): Unit = {
    val jsonString = xs.asJson.noSpaces
    Files.write(Paths.get(filename), jsonString.getBytes)
  }

}
