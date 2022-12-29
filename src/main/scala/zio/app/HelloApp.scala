package zio.app

import java.io.IOException
import scala.collection.immutable.{SortedMap, SortedSet}
import zio.*

enum Positioning {
  case Unknown
  case Absent
  case Mispositioned
  case WellPositioned(positions: SortedSet[Int])

  def merge(that: Positioning): Either[String, Positioning] =
    mergePartial
      .andThen(p => Right(p))
      .applyOrElse((this, that), (p1, p2) => Left(s"Cannot merge $p1 and $p2"))

  private val mergePartial: PartialFunction[(Positioning, Positioning), Positioning] = {
    case (p, Unknown) => p
    case (Unknown, p) => p

    case (Absent, Absent) => Absent

    case (p, Mispositioned) => p
    case (Mispositioned, p) => p

    case (WellPositioned(p1), WellPositioned(p2)) => WellPositioned(p1 ++ p2)
  }
}

case class Thing(letterPositionings: SortedMap[Char, Positioning]) {
  def merge(that: Thing): Thing = {
    val letterPositionings: Seq[(Char, Positioning)] =
      this.letterPositionings.toSeq
      ++ that.letterPositionings.toSeq

    val updatedLetterPositionings: SortedMap[Char, Positioning] =
      letterPositionings
        .groupMapReduce((letter, _) => letter) // Get key
                       ((_, positioning) => positioning) // Get value
                       ((p1, p2) => p1.merge(p2).getOrElse(???)) // Merge 2 values
        .to(SortedMap)

    Thing(updatedLetterPositionings)
  }
}

object Thing {
  def apply(letterPositionings: (Char, Positioning)*) = new Thing(SortedMap.from(letterPositionings))
  val initial: Thing = new Thing(SortedMap.empty[Char, Positioning].withDefault(_ => Positioning.Unknown))
}

val config: URIO[Scope, Int] = ZIO.acquireRelease(ZIO.logInfo("Create") *> ZIO.succeed(1))(_ => ZIO.logInfo("Destroy"))

def check(word: String, guess: String): Thing =
  guess
    .zipWithIndex
    .map((letterGuess, position) => Thing(letterGuess -> check(word, letterGuess, position)))
    .foldLeft(Thing.initial)((acc, positioning) => acc.merge(positioning))

def check(word: String, letterGuess: Char, position: Int): Positioning =
  if word(position) == letterGuess then Positioning.WellPositioned(SortedSet(position))
  else if word.contains(letterGuess) then Positioning.Mispositioned
  else Positioning.Absent

object WordleApp extends ZIOAppDefault {
  def run = for {
    _ <- Console.printLine("Start")
    a = Thing(
      'A' -> Positioning.Unknown,
      'B' -> Positioning.WellPositioned(SortedSet(3))
    )
    b = Thing(
      'A' -> Positioning.Mispositioned,
      'B' -> Positioning.WellPositioned(SortedSet(1, 2))
    )
    c = check("MONTAGNE", "PAPOTTER")
    _ <- Console.printLine(c)
  } yield ()
}

object HelloApp extends ZIOAppDefault {
  def run: ZIO[ZIOAppArgs, IOException, Unit] = for {
    _ <- ZIO.scoped {
      for {
        n <- config
        _ <- Console.printLine(s"A$n").repeat(Schedule.fixed(1.second) && Schedule.recurs(5)) <&> Console.printLine(s"   B$n").repeat(Schedule.fixed(400.millis) && Schedule.recurs(8))
      } yield ()
    }
    args <- getArgs
    letters: SortedSet[Char] = SortedSet.from('A' to 'Z') - 'M'
    test = letters - 'Z'
    _ <- Console.printLine(letters)
    _ <- Console.printLine(s"args=$args")
    _ <- Console.printLine("What's your name?")
    name <- Console.readLine
    _ <- Console.printLine(s"Hello $name!").schedule(Schedule.fixed(1.second) && Schedule.recurs(5)).fork
    _ <- Clock.sleep(3.second + 100.millis)
  } yield ()
}
