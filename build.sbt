name := "trellocat"

version := "1.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http-core" % "2.4.10",
  "com.typesafe.akka" %% "akka-http-testkit" % "2.4.10",
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % "2.4.10",
  "com.github.kxbmap" %% "configs" % "0.4.2",
  "org.scalatest" %% "scalatest" % "2.2.6"
)

scalaVersion := "2.11.8"