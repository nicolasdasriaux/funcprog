package zio.app

import zio.*
import zio.cli.*


def runServer: URIO[HttpServerConfig, Unit] = {
  for {
    config <- ZIO.service[HttpServerConfig]
    _ <- Console.printLine(s"host=${config.host}").orDie
    _ <- Console.printLine(s"port=${config.port}").orDie
  } yield ()
}

val configLayer = HttpServerConfig.layer

object FirstZioApp extends ZIOAppDefault {
  def run = for {
    args <- ZIOAppArgs.getArgs.map(_.toList)
    _ <- helloCliApp.run(args)
    _ <- runServer.provide(configLayer, ZLayer.Debug.tree)
    _ <- Console.printLine("What's you name ?")
    name <- Console.readLine("> ")
    _ <- Console.printLine(s"Hello $name!")
  } yield ()
}
