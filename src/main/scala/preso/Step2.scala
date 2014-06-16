package preso

import akka.actor.ActorSystem
import akka.stream.FlowMaterializer
import akka.stream.MaterializerSettings
import akka.stream.scaladsl.Flow

object Step2 extends App {
  import Bank._

  implicit val sys = ActorSystem("Intro")
  val mat = FlowMaterializer(MaterializerSettings())

  val input = Flow(() ⇒ transfer()).toProducer(mat)
  // make it print one Transfer per second
}