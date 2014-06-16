package demo

import akka.actor.ActorSystem
import akka.stream.FlowMaterializer
import akka.stream.MaterializerSettings
import akka.stream.scaladsl.Flow
import scala.concurrent.duration._

object Demo0 {
  import Bank._

  implicit val sys = ActorSystem("Intro")
  implicit val ec = sys.dispatcher
  implicit val sched = sys.scheduler
  val mat = FlowMaterializer(MaterializerSettings(initialInputBufferSize = 2, maximumInputBufferSize = 2))

  case class Summary(num: Int, amount: Long) {
    def +(t: Transfer) = Summary(num + 1, amount + t.amount)
  }
  object Summary {
    def apply(t: Transfer): Summary = Summary(1, t.amount)
  }

  def main(args: Array[String]): Unit = {
    val input = Flow(() ⇒ Transfer()).toProducer(mat)
    val ticks = Flow(1.second, () ⇒ Tick)

    val rateFlow = Flow(5.seconds, {
      var current = 1.0

      () ⇒ {
        current *= 1.5
        rates(current)
      }
    })

    val summarized = rateFlow.expand(identity, (rates: Map[Currency, Double]) ⇒ rates -> rates).zip(input).map {
      case (rates, t) ⇒ Transfer(t.from, t.to, Currency("EUR"), (t.amount.toDouble / rates(t.currency)).toLong)
    }.conflate[Summary](Summary(_), _ + _).toProducer(mat)

    ticks.zip(summarized).foreach { case (_, Summary(n, amount)) ⇒ println(s"$n transfers, amounting to $amount") }.consume(mat)
  }

  def rates(factor: Double): Map[Currency, Double] = WebService.rates.mapValues(_ * factor)
}