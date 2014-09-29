package preso

import akka.actor.ActorSystem
import akka.stream.FlowMaterializer
import akka.stream.MaterializerSettings
import akka.stream.scaladsl.Flow
import scala.concurrent.duration._

object Step3 extends App {
  import Bank._

  implicit val sys = ActorSystem("Intro")
  implicit val ec = sys.dispatcher
  implicit val sched = sys.scheduler
  val mat = FlowMaterializer(MaterializerSettings())

  val input = Flow(() ⇒ transfer()).toProducer(mat)
  val ticks = Flow(1.second, () ⇒ Tick)
  // ask WebService for currency exchange rate
  ticks.zip(input).mapFuture {
    case (_, t) => WebService.convertToEUR(t.currency, t.amount)
     .map(amount => t -> Transfer(t.from, t.to, Currency("EUR"), amount))
  }.foreach(println).consume(mat)
}









