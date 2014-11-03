import akka.stream.scaladsl.{Zip, PartialFlowGraph, UndefinedSink, Source}

/*package preso

import org.reactivestreams.Publisher
import akka.stream.scaladsl.Flow
import akka.stream.FlowMaterializer
import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.stream.MaterializerSettings

object Merge {

  def apply[T](streams: Seq[Publisher[_ <: T]], mat: FlowMaterializer): Publisher[T] = {
    def rec(level: Int, p: Publisher[T], s: Seq[Publisher[T]]): Publisher[T] =
      if (s.isEmpty) p
      else {
        val toMerge = if (level == 1) s.head else rec(1, s.head, s.tail.take(level - 1))
        rec(level * 2, Flow(p).merge(toMerge).toPublisher(mat), s.drop(level))
      }
    val cast = streams.asInstanceOf[Seq[Publisher[T]]] // due to Java invariance
    rec(1, cast.head, cast.tail)
  }
  
}
          */