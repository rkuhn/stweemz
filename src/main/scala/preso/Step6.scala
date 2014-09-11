package preso

import akka.actor.ActorSystem
import akka.stream.FlowMaterializer
import akka.stream.MaterializerSettings
import akka.stream.scaladsl.Flow
import scala.concurrent.duration._

object Step6 extends App {
  import Bank._

  implicit val sys = ActorSystem("Intro")
  implicit val ec = sys.dispatcher
  implicit val sched = sys.scheduler
  val mat = FlowMaterializer(MaterializerSettings())

  val input = Flow(() ⇒ transfer())
  val ticks = Flow(1000.millis, () ⇒ Tick)

  val streams =
    for (_ ← 1 to 38462)
      yield ticks.
              zip(input.toPublisher(mat)).
              map(x ⇒ x._2).
              mapFuture { t ⇒
               WebService.convertToEUR(t.currency, t.amount).
                          map(eurAmount ⇒
                                t.copy( currency = Currency("EUR"),
                                        amount = eurAmount )
                             )
              }.toPublisher(mat)

  Flow(Merge(streams, mat)).
    groupedWithin(1000000, 1.second).
    map(analyze).
    foreach(println).
    consume(mat)

  private def analyze(transfers: Seq[Transfer]): String = {
    val num = transfers.size
    val avg = transfers.map(_.amount).sum / num
    s"$num transfers averaging $avg EUR"
  }
}