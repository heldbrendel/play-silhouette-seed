name := """play-scala-slick-starter"""
organization := "de.alternativplan"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.1"

resolvers += Resolver.jcenterRepo
resolvers += "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test

libraryDependencies += "net.codingwell" %% "scala-guice" % "4.2.6"
libraryDependencies += "com.iheart" %% "ficus" % "1.4.7"
libraryDependencies += "com.enragedginger" %% "akka-quartz-scheduler" % "1.8.3-akka-2.6.x"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "5.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0",
  "org.postgresql" % "postgresql" % "42.2.12"
)

libraryDependencies += "com.typesafe.play" %% "play-mailer" % "8.0.0"
libraryDependencies += "com.typesafe.play" %% "play-mailer-guice" % "8.0.0"

libraryDependencies ++= Seq(
  "com.mohiva" %% "play-silhouette" % "7.0.0",
  "com.mohiva" %% "play-silhouette-password-bcrypt" % "7.0.0",
  "com.mohiva" %% "play-silhouette-crypto-jca" % "7.0.0",
  "com.mohiva" %% "play-silhouette-persistence" % "7.0.0",
  "com.mohiva" %% "play-silhouette-testkit" % "7.0.0" % "test"
)

libraryDependencies ++= Seq(
  "org.webjars.npm" % "bulma" % "0.8.2",
  "org.webjars" % "font-awesome" % "5.13.0"
)
