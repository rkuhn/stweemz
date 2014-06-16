package demo

import akka.actor.ActorSystem
import akka.stream.FlowMaterializer
import akka.stream.MaterializerSettings
import akka.stream.scaladsl.Flow
import scala.concurrent.duration._

object Intro2 {
  import Bank._

  implicit val sys = ActorSystem("Intro")
  implicit val ec = sys.dispatcher
  implicit val sched = sys.scheduler
  val mat = FlowMaterializer(MaterializerSettings())

  def main(args: Array[String]): Unit = {
    val input = Flow(() ⇒ Transfer()).toProducer(mat)
    val ticks = Flow(1.second, () ⇒ Tick)

    ticks.zip(input).map(x ⇒ x._2).mapFuture { t ⇒
      WebService.convertToEURslow(t.currency, t.amount)
        .map(amount ⇒ t -> Transfer(t.from, t.to, Currency("EUR"), amount))
    }.foreach(println).consume(mat)

  }
}