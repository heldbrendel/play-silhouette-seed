lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := """play-silhouette-slick-seed""",
    version := "1.0",
    scalaVersion := "2.13.1",
    libraryDependencies ++= Seq(
      guice,
      "org.postgresql"          % "postgresql"                      % "42.2.18",
      "com.typesafe.play"      %% "play-slick"                      % "5.0.0",
      "com.typesafe.play"      %% "play-slick-evolutions"           % "5.0.0",
      "com.mohiva"             %% "play-silhouette"                 % "7.0.0",
      "com.mohiva"             %% "play-silhouette-password-bcrypt" % "7.0.0",
      "com.mohiva"             %% "play-silhouette-persistence"     % "7.0.0",
      "com.mohiva"             %% "play-silhouette-crypto-jca"      % "7.0.0",
      "com.typesafe.play"      %% "play-mailer"                     % "8.0.1",
      "com.typesafe.play"      %% "play-mailer-guice"               % "8.0.1",
      "org.scalatestplus.play" %% "scalatestplus-play"              % "5.0.0" % Test
    ),
    resolvers += Resolver.jcenterRepo,
    resolvers += "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings"
    )
  )
