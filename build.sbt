name := "holzer-twitter"

version := "1.0"

scalaVersion := "2.11.4"

libraryDependencies += "net.databinder.dispatch" %% "dispatch-core" % "0.11.2"

libraryDependencies += "net.databinder.dispatch" % "dispatch-json4s-native_2.11" % "0.11.2"

libraryDependencies += "org.specs2" %% "specs2-core" % "2.4.13" % "test"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.7"

libraryDependencies += "com.twitter" %% "chill" % "0.5.1"

libraryDependencies += "org.scala-lang" %% "scala-pickling" % "0.9.0"

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test"

resolvers += "theatr.us" at "http://repo.theatr.us"

libraryDependencies += "us.theatr" %% "akka-quartz" % "0.3.0"

