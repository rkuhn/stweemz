package demo

import org.reactivestreams.api.Producer
import scala.annotation.tailrec

object Merge {

  def apply[T](streams: Seq[Producer[_ <: T]]): Producer[T] = {
    @tailrec def rec(level: Int, s: Seq[Producer[_ <: T]]): Producer[T] = {
      
    }
    rec(1, streams)
  }
  
}