package preso

import akka.actor.ActorSystem
import akka.stream.FlowMaterializer
import akka.stream.MaterializerSettings
import akka.stream.scaladsl.Flow
import scala.concurrent.duration._

case object Tick

object Step3 extends App {
  import Bank._

  implicit val sys = ActorSystem("Intro")
  implicit val ec = sys.dispatcher
  implicit val sched = sys.scheduler
  val mat = FlowMaterializer(MaterializerSettings())

  val input = Flow(() ⇒ transfer()).toProducer(mat)
  val ticks = Flow(1.second, () ⇒ Tick)
  // ask WebService for currency exchange rate
}