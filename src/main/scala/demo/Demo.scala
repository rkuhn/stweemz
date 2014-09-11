package demo

import akka.actor.ActorSystem
import akka.stream.FlowMaterializer
import akka.stream.MaterializerSettings
import akka.stream.scaladsl.Flow
import scala.concurrent.duration._

object Demo {
  import Bank._

  implicit val sys = ActorSystem("Intro")
  implicit val ec = sys.dispatcher
  implicit val sched = sys.scheduler
  val mat = FlowMaterializer(MaterializerSettings(initialInputBufferSize = 2))

  case class Summary(num: Int, amount: Long) {
    def +(t: Transfer) = Summary(num + 1, amount + t.amount)
  }
  object Summary {
    def apply(t: Transfer): Summary = Summary(1, t.amount)
  }

  def main(args: Array[String]): Unit = {
    val input = Flow(() ⇒ Transfer()).toPublisher(mat)
    val ticks = Flow(1.second, () ⇒ Tick)

    val summarized = Flow(input).mapFuture { t ⇒
      WebService.convertToEUR(t.currency, t.amount)
        .map(amount ⇒ Transfer(t.from, t.to, Currency("EUR"), amount))
    }.conflate[Summary](Summary(_), _ + _).toPublisher(mat)

    ticks.zip(summarized).foreach { case (_, Summary(n, amount)) ⇒ println(s"$n transfers, amounting to $amount") }.consume(mat)

  }
}