package error.either

import error.either.Either.{Left, Right}

object EitherApp {
  def main(args: Array[String]): Unit = {
    import Either.*

    {
      case class SavingsAccount(balance: Int) {
        def debit(amount: Int): Either[String, SavingsAccount] =
          if this.balance - amount >= 0 then
            Either.succeed(SavingsAccount(balance = this.balance - amount))
          else
            Either.fail("Cannot be over-debited")

        def credit(amount: Int): Either[String, SavingsAccount] =
          if this.balance + amount <= 500 then
            Either.succeed(SavingsAccount(balance = this.balance + amount))
          else
            Either.fail("Cannot be over-credited")
      }

      object SavingsAccount {
        def transfer(
                      source: SavingsAccount,
                      destination: SavingsAccount,
                      amount: Int
                    ): Either[String, (SavingsAccount, SavingsAccount)] =

          source.debit(amount).flatMap { updatedSource =>
            destination.credit(amount).map { updatedDestination =>
              (updatedSource, updatedDestination)
            }
          }
      }

      val success = SavingsAccount.transfer(source = SavingsAccount(200), destination = SavingsAccount(300), amount = 50)
      assert(success == Right((SavingsAccount(150), SavingsAccount(350))))

      val overDebited = SavingsAccount.transfer(source = SavingsAccount(40), destination = SavingsAccount(300), amount = 50)
      assert(overDebited == Left("Cannot be over-debited"))

      val overCredited = SavingsAccount.transfer(source = SavingsAccount(200), destination = SavingsAccount(400), amount = 150)
      assert(overCredited == Left("Cannot be over-credited"))
    }

    {
      case class SavingsAccount(balance: Int) {
        def debit(amount: Int): Either[String, SavingsAccount] =
          if this.balance - amount >= 0 then
            Either.succeed(SavingsAccount(balance = this.balance - amount))
          else
            Either.fail("Cannot be over-debited")

        def credit(amount: Int): Either[String, SavingsAccount] =
          if this.balance + amount <= 500 then
            Either.succeed(SavingsAccount(balance = this.balance + amount))
          else
            Either.fail("Cannot be over-credited")
      }

      object SavingsAccount {
        def transfer(
                      source: SavingsAccount,
                      destination: SavingsAccount,
                      amount: Int
                    ): Either[String, (SavingsAccount, SavingsAccount)] =

          for {
            updatedSource <- source.debit(amount)
            updatedDestination <- destination.credit(amount)
          } yield (updatedSource, updatedDestination)
      }

      val success = SavingsAccount.transfer(source = SavingsAccount(200), destination = SavingsAccount(300), amount = 50)
      assert(success == Right((SavingsAccount(150), SavingsAccount(350))))

      val overDebited = SavingsAccount.transfer(source = SavingsAccount(40), destination = SavingsAccount(300), amount = 50)
      assert(overDebited == Left("Cannot be over-debited"))

      val overCredited = SavingsAccount.transfer(source = SavingsAccount(200), destination = SavingsAccount(400), amount = 150)
      assert(overCredited == Left("Cannot be over-credited"))
    }
  }
}
