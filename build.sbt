val zioVersion = SettingKey[String]("zioVersion")
val zioConfigVersion = SettingKey[String]("zioConfigVersion")
val zioCliVersion = SettingKey[String]("zioCliVersion")
val zioHttpVersion = SettingKey[String]("zioHttpVersion")

lazy val root = (project in file("."))
  .settings(
    name := "funcprog",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "3.2.0",
    scalacOptions ++= Seq("-deprecation", "-Yexplicit-nulls"),
    resolvers += Resolver.sonatypeRepo("snapshots"),
    zioVersion := "2.0.4",
    zioConfigVersion := "3.0.1",
    zioCliVersion := "0.2.7",
    zioHttpVersion := "2.0.0-RC11",

    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion.value,
      "dev.zio" %% "zio-streams" % zioVersion.value,
      "dev.zio" %% "zio-test" % zioVersion.value % Test,

      "dev.zio" %% "zio-config" % zioConfigVersion.value,
      "dev.zio" %% "zio-config-typesafe" % zioConfigVersion.value,
      "dev.zio" %% "zio-config-magnolia" % zioConfigVersion.value,

      "dev.zio" %% "zio-cli" % zioCliVersion.value,

      "io.d11" %% "zhttp" % zioHttpVersion.value,

      "com.h2database" % "h2" % "2.0.202"
    )
  )
