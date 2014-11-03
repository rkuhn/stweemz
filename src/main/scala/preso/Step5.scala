package preso

import akka.actor.ActorSystem
import akka.stream.FlowMaterializer
import akka.stream.MaterializerSettings
import akka.stream.scaladsl._
import scala.concurrent.duration._

object Step5 extends App {
  import akka.preso.Bank._
  implicit val sys = ActorSystem("Step5")
  implicit val mat = FlowMaterializer(MaterializerSettings(sys).withInputBuffer(1,1))
  implicit val sch = sys.scheduler
  implicit val dis = sys.dispatcher

  case class Summary(num: Int, amount: Long) {
    def +(t: Transfer) = Summary(num + 1, amount + t.amount)
  }
  object Summary {
    def apply(t: Transfer): Summary = Summary(1, t.amount)
  }

  val inputs = Source(Iterator.continually(randomTransfer()))
  val rateFlow =
    Source(0.seconds, 5.seconds, {
      var current = 1.0
      def rates(factor: Double): Map[Currency, Double] = WebService.rates.mapValues(_ * factor)
      () => {
        val r = rates(current)
        current *= 1.5
        r
      }
    })

  // expand rates and use them to convert Transfers
  val summarized = rateFlow.expand(identity, (r: Map[Currency, Double]) => (r, r)).zip(inputs)
    .map { case (rates, t) => Transfer(t.from, t.to, Currency("EUR"), (t.amount / rates(t.currency)).toLong) }
    .conflate[Summary](Summary(_), _ + _)

  Source(0.seconds, 1.second, () => Tick).
    zip(summarized).
    foreach { case (_, Summary(n, amount)) ⇒ println(s"$n transfers, amounting to $amount") }.
    onComplete {
      _ => sys.shutdown()
    }
}
