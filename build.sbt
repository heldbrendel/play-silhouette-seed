name := "play-silhouette-slick-seed"

version := "6.1.1"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.0"

resolvers += Resolver.jcenterRepo

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  "com.mohiva" %% "play-silhouette"                 % "6.1.1",
  "com.mohiva" %% "play-silhouette-password-bcrypt" % "6.1.1",
  "com.mohiva" %% "play-silhouette-persistence"     % "6.1.1",
  "com.mohiva" %% "play-silhouette-crypto-jca"      % "6.1.1",

  "com.typesafe.play" %% "play-slick"             % "4.0.2",
  "com.typesafe.play" %% "play-slick-evolutions"  % "4.0.2",

  "net.codingwell" %% "scala-guice" % "4.2.6",

  "com.iheart" %% "ficus" % "1.4.7",

  "com.typesafe.play" %% "play-mailer"        % "7.0.1",
  "com.typesafe.play" %% "play-mailer-guice"  % "7.0.1",

  "com.enragedginger" %% "akka-quartz-scheduler" % "1.8.2-akka-2.6.x",

  "com.mohiva" %% "play-silhouette-testkit" % "6.1.1" % "test",

  "com.h2database" % "h2" % "1.4.200",

  specs2 % Test,
  caffeine,
  guice,
  filters
)

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-Xfatal-warnings"
)