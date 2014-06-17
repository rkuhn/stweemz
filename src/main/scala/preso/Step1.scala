package preso

import akka.actor.ActorSystem
import akka.stream.FlowMaterializer
import akka.stream.MaterializerSettings
import akka.stream.scaladsl.Flow

object Step1 extends App {
  import Bank._
  
  // create stream of Transfers and print it
  implicit val sys = ActorSystem("Step1")
  val mat = FlowMaterializer(MaterializerSettings())
  
  Flow(() => transfer()).map(identity).foreach(println).onComplete(mat)(_ => sys.shutdown())
}