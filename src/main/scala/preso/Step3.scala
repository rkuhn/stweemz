package preso

import akka.actor.ActorSystem
import akka.stream.FlowMaterializer
import akka.stream.MaterializerSettings
import akka.stream.scaladsl._
import scala.concurrent.duration._

object Step3 extends App {
  import akka.preso.Bank._
  implicit val sys = ActorSystem("Step3")
  implicit val mat = FlowMaterializer(MaterializerSettings(sys))
  implicit val sch = sys.scheduler
  implicit val dis = sys.dispatcher

  Source(Iterator.continually(randomTransfer())).
    zip(Source(0.seconds, 1.second, () => Tick)).
    mapAsync { case (t, _) => WebService.convertToEURslow(t) }.
    foreach(println).
    onComplete { _ => sys.shutdown() }
}