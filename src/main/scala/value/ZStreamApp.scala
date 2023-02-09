package value

import zio.*
import zio.stream.*

object ZStreamApp extends ZIOAppDefault {
  override def run = {
    case class Customer(id: Int, firstName: String, lastName: String)

    val idsStream: ZStream[Any, Nothing, Int] = ZStream.iterate(1)(_ + 1)
    val firstNamesStream: ZStream[Any, Nothing, String] = ZStream("Paul", "Peter", "Mary", "John")
    val lastNamesStream: ZStream[Any, Nothing, String] = ZStream("Simpson", "Davis", "Parker")

    val customersStream: ZStream[Any, Nothing, Customer] =
      idsStream.zip(firstNamesStream.forever).zip(lastNamesStream.forever)
        .map(Customer(_, _, _))
        .take(6)

    val getCustomers: ZIO[Any, Nothing, Chunk[Customer]] =
      customersStream.runCollect

    for {
      customers <- getCustomers
      _ <- Console.printLine(s"customers=$customers")
    } yield ()
  }
}
