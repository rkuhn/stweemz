package preso

import akka.actor.ActorSystem
import akka.stream.FlowMaterializer
import akka.stream.MaterializerSettings
import akka.stream.scaladsl.Flow
import scala.concurrent.duration._

object Step4 extends App {
  import Bank._

  case class Summary(num: Int, amount: Long) {
    def +(t: Transfer) = Summary(num + 1, amount + t.amount)
  }
  object Summary {
    def apply(t: Transfer): Summary = Summary(1, t.amount)
  }

  implicit val sys = ActorSystem("Intro")
  implicit val ec = sys.dispatcher
  implicit val sched = sys.scheduler
  val mat = FlowMaterializer(MaterializerSettings(initialInputBufferSize = 2))

  val input = Flow(() ⇒ transfer()).toProducer(mat)
  val ticks = Flow(1.second, () ⇒ Tick)
  // convert to EUR, then conflate and print one per second
  val summarized = Flow(input).mapFuture(t => WebService.convertToEUR(t.currency, t.amount)
    .map(Transfer(t.from, t.to, Currency("EUR"), _))  
  ).conflate[Summary](Summary(_), _ + _).toProducer(mat)
  
  ticks.zip(summarized).foreach(println).consume(mat)
}











