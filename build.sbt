name := "holzer-twitter"

version := "2.0"

scalaVersion := "2.12.12"

libraryDependencies += "org.specs2" %% "specs2-core" % "4.10.6" % "test"

val AkkaVersion = "2.6.12"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % AkkaVersion % Test,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test
)

libraryDependencies += "org.scalatest" % "scalatest_2.12" % "3.2.15" % "test"

libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.3.6"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core" % "0.14.1",
  "io.circe" %% "circe-generic" % "0.14.1",
  "io.circe" %% "circe-parser" % "0.14.1"
)

libraryDependencies += "com.danielasfregola" %% "twitter4s" % "7.1"
