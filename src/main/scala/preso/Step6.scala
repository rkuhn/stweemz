package preso

import akka.actor.ActorSystem
import akka.stream.FlowMaterializer
import akka.stream.MaterializerSettings
import akka.stream.scaladsl._
import scala.concurrent.duration._

object Step6 extends App {
  import akka.preso.Bank._
  implicit val sys = ActorSystem("Step6")
  implicit val mat = FlowMaterializer(MaterializerSettings(sys).withInputBuffer(1,1))
  implicit val sch = sys.scheduler
  implicit val dis = sys.dispatcher

  val inputs = Source(Iterator.continually(randomTransfer()))
  val ticks = Source(0.seconds, 1.second, () => Tick)
  val upstream = ticks.zip(inputs).map(x ⇒ x._2).mapAsync(WebService.convertToEUR(_))

  upstream.mergeX(1 to 38461 map { _ => upstream }: _*).
    groupedWithin(1000000, 1.second).
    map(analyze).
    foreach(println).
    onComplete { _ =>
      sys.shutdown()
    }

  private def analyze(transfers: Seq[Transfer]): String = {
    val num = transfers.size
    val avg = transfers.map(_.amount).sum / num
    s"$num transfers averaging $avg EUR"
  }
}