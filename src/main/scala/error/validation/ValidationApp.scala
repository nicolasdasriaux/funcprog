package error.validation

object ValidationApp {
  case class SavingsAccount(balance: Int) {
    def debit(amount: Int): Validation[String, SavingsAccount] =
      if this.balance - amount >= 0 then
        Validation.succeed(SavingsAccount(balance = this.balance - amount))
      else
        Validation.fail("Cannot be over-debited")

    def credit(amount: Int): Validation[String, SavingsAccount] =
      if this.balance + amount <= 500 then
        Validation.succeed(SavingsAccount(balance = this.balance + amount))
      else
        Validation.fail("Cannot be over-credited")
  }

  case class TransferResult(updatedSource: SavingsAccount, updatedDestination: SavingsAccount)

  object SavingsAccount {
    def transfer(source: SavingsAccount, destination: SavingsAccount, amount: Int): Validation[String, TransferResult] = {
      val updatedSource = source.debit(amount)
      val updatedDestination = destination.credit(amount)
      val updatedAccounts: Validation[String, (SavingsAccount, SavingsAccount)] = updatedSource <&> updatedDestination

      val transferResult: Validation[String, TransferResult] =
        updatedAccounts.map((updatedSource, updatedDestination) => TransferResult(updatedSource, updatedDestination))

      transferResult
    }
  }

  def main(args: Array[String]): Unit = {
    import Validation.*

    val overCreditedAndOverDebited = SavingsAccount.transfer(source = SavingsAccount(40), destination = SavingsAccount(400), amount = 150)
    assert(overCreditedAndOverDebited == Failure(Seq("Cannot be over-debited", "Cannot be over-credited")))
  }
}
