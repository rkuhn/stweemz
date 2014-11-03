package preso

import akka.actor.ActorSystem
import akka.stream.FlowMaterializer
import akka.stream.MaterializerSettings
import akka.stream.scaladsl._

object Step1 extends App {
  import akka.preso.Bank._
  implicit val sys = ActorSystem("Step1")
  implicit val mat = FlowMaterializer(MaterializerSettings(sys))
  import sys.dispatcher

  Source(Iterator.continually(randomTransfer())).
    filter(_.amount > 900).
    take(10000).
    foreach(println).
    onComplete { _ => sys.shutdown() }
}