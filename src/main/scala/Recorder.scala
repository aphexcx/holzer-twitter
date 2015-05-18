import java.nio.file.{Files, Paths}

import scala.pickling._
import scala.pickling.binary._
import scala.util.Random

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

  def unused(quotes: Seq[String], used: Set[String]): Option[String] =
    Random.shuffle(quotes filterNot used).headOption

  def getAnUnusedQuote(quotes: Seq[String]): String =
    unused(quotes, used).getOrElse {
      reset()
      unused(quotes, used).get
    }

  def markUsed(s: String) = dump(use(s, used))

  def reset() = dump(Set.empty[String])

}