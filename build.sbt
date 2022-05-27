val zioVersion = SettingKey[String]("zioVersion")
val zioHttpVersion = SettingKey[String]("zioHttpVersion")

lazy val root = (project in file("."))
  .settings(
    name := "funcprog",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "3.1.1",
    scalacOptions ++= Seq("-deprecation", "-Yexplicit-nulls"),
    resolvers += Resolver.sonatypeRepo("snapshots"),
    zioVersion := "2.0.0-RC6",
    zioHttpVersion := "2.0.0-RC6",

    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion.value,
      "dev.zio" %% "zio-streams" % zioVersion.value,
      "dev.zio" %% "zio-test" % zioVersion.value % Test,

      "io.d11" %% "zhttp" % zioHttpVersion.value,
      "io.d11" %% "zhttp-test" % zioHttpVersion.value % Test,

      "com.h2database" % "h2" % "2.0.202"
    )
  )
