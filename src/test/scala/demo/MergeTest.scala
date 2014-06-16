package demo

import akka.actor.ActorSystem
import org.junit.BeforeClass
import org.junit.AfterClass
import org.junit.Test
import akka.stream.scaladsl.Flow
import akka.stream.FlowMaterializer
import akka.stream.MaterializerSettings
import org.reactivestreams.api.Producer
import scala.concurrent.Await
import scala.concurrent.duration._
import org.hamcrest.Matchers._
import org.hamcrest.MatcherAssert._

object MergeTest {

  implicit var sys: ActorSystem = _
  lazy val mat = FlowMaterializer(MaterializerSettings())

  @BeforeClass def start {
    sys = ActorSystem("MergeTest")
  }

  @AfterClass def stop {
    sys.shutdown()
  }

  implicit class ts[T](val p: Producer[T]) extends AnyVal {
    def toSeq = Await.result(Flow(p).grouped(Int.MaxValue).toFuture(mat), 30.seconds)
  }

}

class MergeTest {

  import MergeTest._

  @Test def mergeOne {
    assert(Merge(Seq(flow(1)), mat).toSeq == seq(1))
  }

  @Test def mergeTwo {
    assert(Merge(Seq(flow(1), flow(2)), mat).toSeq.sorted == seq(1) ++ seq(2))
  }

  @Test def mergeMany {
    assertThat(Merge((1 to 11) map flow, mat).toSeq.groupBy(identity).map(x ⇒ x._1 -> x._2.size), equalTo((1 to 11).map(_ -> 10).toMap))
  }

  @Test def mergeManyMore {
    val first = Merge((1 to 100000) map flow, mat).toSeq.groupBy(identity).map(x ⇒ x._1 -> x._2.size)
    val second = (1 to 100000).map(_ -> 10).toMap
    compare(second, first)
    assertThat(first, equalTo(second))
  }

  private def flow(x: Int) = Flow(seq(x)).toProducer(mat)
  private def seq(x: Int) = (1 to 10) map (_ ⇒ x)

  private def compare[K, V](left: Map[K, V], right: Map[K, V]) {
    val surplus = right.filterNot { case (k, v) ⇒ left.get(k) == Some(v) }
    val missing = left.filterNot { case (k, v) => right.get(k) == Some(v) }
    println(s"surplus: $surplus\n\nmissing: $missing")
  }

}