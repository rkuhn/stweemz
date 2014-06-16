package preso

import akka.actor.ActorSystem
import akka.stream.FlowMaterializer
import akka.stream.MaterializerSettings
import akka.stream.scaladsl.Flow
import scala.concurrent.duration._
import org.reactivestreams.api.Producer

object Step5 extends App {
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
  
  // flow of rates
  
  // expand rates and use them to convert Transfers
  val summarized: Producer[Summary] = ???
  
  ticks.zip(summarized).foreach { case (_, Summary(n, amount)) ⇒ println(s"$n transfers, amounting to $amount") }.consume(mat)


  def rates(factor: Double): Map[Currency, Double] = WebService.rates.mapValues(_ * factor)
}