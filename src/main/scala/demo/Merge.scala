package demo

import org.reactivestreams.api.Producer
import akka.stream.scaladsl.Flow
import akka.stream.FlowMaterializer
import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.stream.MaterializerSettings

object Merge {

  def apply[T](streams: Seq[Producer[_ <: T]], mat: FlowMaterializer): Producer[T] = {
    def rec(level: Int, p: Producer[T], s: Seq[Producer[T]]): Producer[T] =
      if (s.isEmpty) p
      else {
        val toMerge = if (level == 1) s.head else rec(1, s.head, s.tail.take(level - 1))
        rec(level * 2, Flow(p).merge(toMerge).toProducer(mat), s.drop(level))
      }
    val cast = streams.asInstanceOf[Seq[Producer[T]]] // due to Java invariance
    rec(1, cast.head, cast.tail)
  }
  
}

object MergeApp extends App {
  implicit val sys = ActorSystem("MergeApp")
  val mat = FlowMaterializer(MaterializerSettings())
  val f = Flow(3.seconds, () => 1).foreach(println).consume(mat)
  val fs = for (i <- 2 to 11) yield Flow(2.seconds, () => i).toProducer(mat)
  val ffs = Merge(fs, mat)
  Flow(ffs).foreach(println).consume(mat)
}