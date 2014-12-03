package dispatch.as.repatch.twitter.response

package object stream {

  import dispatch.as.json4s.stream.Json
  import org.json4s._
  import twitter.{response => r}

  def TweetOrJson[A](f: Either[JValue, r.Tweet] => A) = Json[A] {
    case r.Tweet(tweet) => f(Right(tweet))
    case json => f(Left(json))
  }
}