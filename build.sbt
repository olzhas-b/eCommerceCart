name := "Backend"

version := "0.1"

scalaVersion := "2.13.4"
val akkaVersion = "2.6.10"
libraryDependencies ++= Seq(
   "com.typesafe.akka" %% "akka-persistence-typed" % akkaVersion,
   "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion,
   "ch.qos.logback" % "logback-classic" % "1.2.3",
   "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
   "org.scalatest" %% "scalatest" % "3.1.0" % Test
)

val circeVersion = "0.13.0"
val akkaHttpVersion = "10.2.1"
libraryDependencies ++= Seq(
   "com.typesafe.akka" %% "akka-actor-typed"           % akkaVersion,
   "com.typesafe.akka" %% "akka-cluster-typed"         % akkaVersion,
   "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion,
   "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
   "com.typesafe.akka" %% "akka-stream" % akkaVersion,
   "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,

   "ch.qos.logback"    %  "logback-classic"             % "1.2.3",
   "com.typesafe.akka" %% "akka-multi-node-testkit"    % akkaVersion,
   "com.typesafe.akka" %% "akka-actor-testkit-typed"   % akkaVersion,

   "io.circe" %% "circe-core" % circeVersion,
   "io.circe" %% "circe-generic" % circeVersion,
   "io.circe" %% "circe-parser" % circeVersion,
   "de.heikoseeberger" %% "akka-http-circe" % "1.31.0"
)
//GATLING
//enablePlugins(GatlingPlugin)
//
//libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.2.2" % "test"
//
//libraryDependencies += "io.gatling" % "gatling-test-framework" % "2.2.2" % "test"
////GATLING



scalacOptions in Compile ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlog-reflective-calls", "-Xlint")

// show full stack traces and test case durations
testOptions in Test += Tests.Argument("-oDF")
logBuffered in Test := false

licenses := Seq(("CC0", url("http://creativecommons.org/publicdomain/zero/1.0")))

mainClass in Compile := Some("Main")