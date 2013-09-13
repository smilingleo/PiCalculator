name := "Distributed PI Calculator"

version := "1.0"

scalaVersion := "2.10.2"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/release/"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.2.1"

libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.2.1"

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test"

initialCommands in console := """import akka.actor._; implicit val system = ActorSystem("demo")"""
