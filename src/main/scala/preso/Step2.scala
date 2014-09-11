package preso

import akka.actor.ActorSystem
import akka.stream.FlowMaterializer
import akka.stream.MaterializerSettings
import akka.stream.scaladsl.Flow
import scala.concurrent.duration._

case object Tick

object Step2 extends App {
  import Bank._

  implicit val sys = ActorSystem("Intro")
  val mat = FlowMaterializer(MaterializerSettings())

  val input = Flow(() ⇒ transfer()).toPublisher(mat)
  // rate limit to one Transfer per second and print it
  val ticks = Flow(1.second, () => Tick)
  
  ticks.zip(input).foreach(println).consume(mat)
}