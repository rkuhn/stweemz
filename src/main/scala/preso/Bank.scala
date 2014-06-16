package preso

import scala.concurrent.forkjoin.ThreadLocalRandom.{ current => rnd }

object Bank {
  case class Account(name: String) extends AnyVal { override def toString = s"'$name'" }
  case class Currency(name: String) extends AnyVal { override def toString = s"'$name'" }
  case class Transfer(from: Account, to: Account, currency: Currency, amount: Long)

  private val currencies = Array("EUR", "USD", "SEK", "PLN", "LTL", "HUF")
  def currency(): Currency = Currency(currencies(rnd.nextInt(currencies.length)))
  def account(): Account = Account((1 to 4) map (_ ⇒ ('A' + rnd.nextInt(26)).toChar) mkString "")
  def transfer(): Transfer = Transfer(account(), account(), currency(), rnd.nextLong(1000))
}