package zio.layer

import zio.*

import java.io.IOException

case class Connection(id: Int) {
  def close: ZIO[Any, Nothing, Unit] = Console.printLine(s"Closing connection $id").orDie
}

case class Database(ref: Ref[Int]) {
  def open: ZIO[Any, Nothing, Connection] = {
    for {
      id <- ref.getAndUpdate(_ + 1)
      _ <- Console.printLine(s"Opening connection $id").orDie
    } yield Connection(id)
  }

  def make: ZIO[Scope, Nothing, Connection] = ZIO.acquireRelease(open)(_.close)
}

case class Customer(id: Int, firstName: String, lastName: String)

class CustomerRepository {
  def findAll: ZIO[Connection, Nothing, Seq[Customer]] =
    ZIO.succeed(for (id <- 1 to 5) yield Customer(id, s"First Name $id", s"Last Name $id"))

  def insert(customer: Customer): ZIO[Connection, Nothing, Customer] = ZIO.succeed(customer)
  def delete(customer: Customer): ZIO[Connection, Nothing, Unit] = ZIO.unit
}

object CustomerRepository {
  def layer: ZLayer[Any, Nothing, CustomerRepository] = ZLayer.succeed(new CustomerRepository)
}

object Database {
  def layer: ZLayer[Any, Nothing, Database] = ZLayer(Ref.make(1).map(ref => Database(ref)))
}

object LayerApp extends ZIOAppDefault {
  val program: ZIO[Database & CustomerRepository, IOException, Unit] = ZIO.scoped {
    for {
      database <- ZIO.service[Database]
      customerRepository <- ZIO.service[CustomerRepository]
      connections <- ZIO.foreach(1 to 10)(_ => database.make)
      connection = connections(0)

      customers <- {
        for {
          customers <- customerRepository.findAll.debug("customers")
          newCustomer <- customerRepository.insert(Customer(10, "Paul", "Verlaine")).debug("newCustomer")
        } yield ()
      }.provideLayer(ZLayer.succeed(connection))
    } yield ()
  }

  override def run: ZIO[Any, Any, Any] = program.provide(Database.layer, CustomerRepository.layer)
}
