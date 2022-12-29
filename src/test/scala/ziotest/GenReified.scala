package ziotest

import zio.*
import zio.test.*

import java.time.LocalDate

object GenReified {
  def main(args: Array[String]): Unit = {
    val idGen: Gen[Any, Int] = Gen.int(min = 1, max = 5000)
    val firstNameGen: Gen[Any, String] = Gen.elements("Peter", "Paul", "Mary")
    val lastNameGen: Gen[Any, String] = Gen.elements("Brown", "Jones", "Miller", "Davis")

    val birthDateGen: Gen[Any, LocalDate] = Gen.localDate(
      min = LocalDate.of(1950, 1, 1).nn,
      max = LocalDate.of(1995, 12, 31).nn
    )

    case class Person(id: Int, firstName: String, lastName: String, birthDate: LocalDate)

    val personGen: Gen[Any, Person] =
      idGen.zip(firstNameGen).zip(lastNameGen).zip(birthDateGen)
        .map(Person(_, _, _, _))

    val generatePeople: UIO[List[Person]] = personGen.runCollectN(10)

    val peopleExit: Exit[Nothing, List[Person]] = Unsafe.unsafe { unsafe ?=>
      Runtime.default.unsafe.run(generatePeople)
    }
  }
}
