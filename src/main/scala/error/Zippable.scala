package error

import scala.util.NotGiven

trait Zippable[-A, -B] {
  type Out
  def zip(left: A, right: B): Out
}

object Zippable {
  type Out[-A, -B, C] = Zippable[A, B] { type Out = C }

  given unitZipUnit[A]: Zippable.Out[Unit, Unit, Unit] = new Zippable[Unit, Unit] {
    type Out = Unit
    def zip(left: Unit, right: Unit): Out = ()
  }

  given anyZipUnit[A]: Zippable.Out[A, Unit, A] = new Zippable[A, Unit] {
    type Out = A
    def zip(left: A, right: Unit): Out = left
  }

  given unitZipAny[B]: Zippable.Out[Unit, B, B] = new Zippable[Unit, B] {
    type Out = B
    def zip(left: Unit, right: B): Out = right
  }

  given tupleZipTuple[A <: Tuple, B <: Tuple]: Zippable.Out[A, B, Tuple.Concat[A, B]] =
    new Zippable[A, B] {
      type Out = Tuple.Concat[A, B]
      def zip(left: A, right: B): Out = left ++ right
    }

  given valueZipTuple[A, B <: Tuple](using NotGiven[A <:< Tuple]): Zippable.Out[A, B, A *: B] =
    new Zippable[A, B] {
      type Out = A *: B
      def zip(left: A, right: B): Out = left *: right
    }

  given tupleZipValue[A <: Tuple, B](using NotGiven[B <:< Tuple]): Zippable.Out[A, B, Tuple.Concat[A, Tuple1[B]]] =
    new Zippable[A, B] {
      type Out = Tuple.Concat[A, Tuple1[B]]
      def zip(left: A, right: B): Out = left ++ Tuple(right)
    }

  given valueZipValue[A, B](using NotGiven[A <:< Tuple], NotGiven[B <:< Tuple]): Zippable.Out[A, B, (A, B)] =
    new Zippable[A, B] {
      type Out = (A, B)
      def zip(left: A, right: B): Out = (left, right)
    }
}
