package zio.ziohttp

import zhttp.http.*
import zhttp.http.middleware.HttpMiddleware
import zhttp.service.Server
import zio.*

import scala.util.Try

case class Person(id: Int, firstName: String, lastName: String)

object SimpleHttpApp extends ZIOAppDefault {
  val app: HttpApp[Any, Nothing] = Http.collectHttp[Request] {
    case Method.GET -> !! / "customers" => value3.catchAll(e => Http.succeed(Response(status = Status.BadGateway, body = Body.fromCharSequence(e))))
  }

  val middleware: HttpMiddleware[Any, Nothing] = Middleware.addHeader("A" -> "Test")
  private val value1: Middleware[Any, String, Int, Person, Request, Response] = Middleware.codec(
    decoder = { (request: Request) =>
      request.url.queryParams
        .get("id").toRight("Missing ID")
        .flatMap(ids => if (ids.size == 1) Right(ids.head) else Left("Expecting exactly 1 value"))
        .flatMap(s => Try(s.toInt).toOption.toRight("Invalid integer"))
    },
    encoder = (person: Person) => Right(Response.text(person.toString))
  )

  private val value2: Http[Any, Nothing, Int, Person] = Http.collect[Int] {
    case id if id > 0 => Person(id, s"First Name $id", s"Last Name $id")
  }
  private val value3: Http[Any, String, Request, Response] = value2 @@ value1

  private val value: Http[Any, Nothing, Nothing, String] = Http.succeed(Person(1, "Paul", "Simon")) @@ Middleware.identity.map((person: Person) => person.toString)

  override val run =
    Server.start(8090, app @@ middleware)
}
