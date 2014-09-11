package demo

import akka.stream.scaladsl.{ Flow, Duct }
import akka.stream.io.{ StreamTcp }
import scala.util.Random
import scala.concurrent.forkjoin.ThreadLocalRandom.{ current => rnd }
import akka.actor.ActorSystem
import akka.stream.FlowMaterializer
import akka.stream.MaterializerSettings
import scala.concurrent.duration._
  
object Bank {
  case class Account (name: String) extends AnyVal {
    override def toString = s"'$name'"
  }
  object Account {
    def apply(): Account = Account((1 to 4) map (_ => ('A' + rnd.nextInt(26)).toChar) mkString "")
  }
  case class Currency (name: String) extends AnyVal {
    override def toString = s"'$name'"
  }
  object Currency {
    private val currencies = Array("EUR", "USD", "SEK", "PLN", "LTL", "HUF")
    def apply(): Currency = Currency(currencies(rnd.nextInt(currencies.length)))
  }
  case class Transfer(from: Account, to: Account, currency: Currency, amount: Long)
  object Transfer {
    def apply(): Transfer = Transfer(Account(), Account(), Currency(), rnd.nextLong(1000))
  }
}

case object Tick
  
object Intro {
  import Bank._
  
  implicit val sys = ActorSystem("Intro")
  val mat = FlowMaterializer(MaterializerSettings())
  
	def main(args: Array[String]): Unit = {
	  val input = Flow(() => Transfer()).toPublisher(mat)
	  val ticks = Flow(1.second, () => Tick)
	  
	  ticks.zip(input).foreach(println).consume(mat)
	}
}