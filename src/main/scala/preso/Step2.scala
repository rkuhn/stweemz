package preso

import akka.actor.ActorSystem
import akka.stream.FlowMaterializer
import akka.stream.MaterializerSettings
import akka.stream.scaladsl._
import scala.concurrent.duration._

case object Tick

object Step2 extends App {
  import akka.preso.Bank._
  implicit val sys = ActorSystem("Step2")
  implicit val mat = FlowMaterializer(MaterializerSettings(sys))
  import sys.dispatcher

  Source(Iterator.continually(randomTransfer())).
    zip(Source(0.seconds, 1.second, () => Tick)).
    take(10).
    foreach(println).
    onComplete { _ => sys.shutdown() }
}