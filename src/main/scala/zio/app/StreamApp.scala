package zio.app

import zio.{ZIO, ZIOAppDefault, Chunk}
import zio.stream.*

object StreamApp extends ZIOAppDefault {
  override def run =
    ZStream.fromFileName("build.sbt")
      .via(
        ZPipeline.utf8Decode
          >>> ZPipeline.splitLines
          >>> ZPipeline.filter[String](_.contains("val"))
          >>> ZPipeline.utf8Encode
      )
      .run(ZSink.fromFileName("clinew.scala"))
  }
