import sbt.Keys.libraryDependencies

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.2.2"

lazy val root = (project in file("."))
  .settings(
    name := "scala-app",
    organization := "com.innowise-group",
  )

val akkaVersion = "2.8.1-M1"
val akkaHttpVersion = "10.5.0"
val jacksonVersion = "2.15.0-rc3"
val ioJsonVersion = "1.3.6"
val slf4jVersion = "2.0.5"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.play" %% "play-json" % "2.10.0-RC7",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion,
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % jacksonVersion,
  "io.spray" %% "spray-json" % ioJsonVersion,
  "org.slf4j" % "slf4j-simple" % slf4jVersion
)
