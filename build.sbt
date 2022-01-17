lazy val root = (project in file("."))
  .settings(
    name := "funcprog",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "3.1.0",
    scalacOptions ++= Seq("-deprecation", "-Yexplicit-nulls"),
    resolvers += Resolver.sonatypeRepo("snapshots"),
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.0.0-RC1",
      "dev.zio" %% "zio-streams" % "2.0.0-RC1",
      "com.h2database" % "h2" % "2.0.202"
    )
  )
