package preso

import akka.actor.ActorSystem
import akka.stream.FlowMaterializer
import akka.stream.MaterializerSettings
import akka.stream.scaladsl._
import scala.concurrent.duration._

object Step4 extends App {
  import akka.preso.Bank._
  implicit val sys = ActorSystem("Step4")
  implicit val mat = FlowMaterializer(MaterializerSettings(sys))
  implicit val sch = sys.scheduler
  implicit val dis = sys.dispatcher

  case class Summary(num: Int, amount: Long) {
    def +(t: Transfer) = Summary(num + 1, amount + t.amount)
  }

  object Summary {
    def apply(t: Transfer): Summary = Summary(1, t.amount)
  }

  Source(Iterator.continually(randomTransfer())).
    mapAsync(WebService.convertToEUR(_)).
    conflate[Summary](Summary(_), _ + _).
    zip(Source(0.seconds, 1.second, () => Tick)).
    foreach { case (Summary(num, amount), _) => println(s"$num transfers worth $amount") }.
    onComplete { _ => sys.shutdown() }
}