package zio.app

import zio.*
import zio.config.*
import zio.config.magnolia.descriptor
import zio.config.typesafe.TypesafeConfigSource

case class HttpServerConfig(host: String, port: Int)

object HttpServerConfig {
  val layer = ZLayer {
    read {
      descriptor[HttpServerConfig].from(
        TypesafeConfigSource.fromResourcePath.at(PropertyTreePath.$("http.server"))
      )
    }
  }
}
