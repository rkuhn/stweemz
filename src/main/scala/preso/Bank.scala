package akka.preso

import java.util.concurrent.atomic.AtomicLong

import akka.actor.{ActorRef, Props, ActorSystem}
import akka.stream.{MaterializerSettings, FlowMaterializer}
import akka.stream.scaladsl._
import akka.stream.scaladsl.FlowGraphImplicits._

import scala.concurrent.forkjoin.ThreadLocalRandom.{ current => rnd }

object Bank {
  case class Account(name: String) extends AnyVal { override def toString = s"'$name'" }
  case class Currency(name: String) extends AnyVal { override def toString = s"'$name'" }
  case class Transfer(from: Account, to: Account, currency: Currency, amount: Long)

  private val currencies = Array("EUR", "USD", "SEK", "PLN", "LTL", "HUF")
  def randomCurrency(): Currency = Currency(currencies(rnd.nextInt(currencies.length)))
  def randomAccount(): Account = Account((1 to 4) map (_ ⇒ ('A' + rnd.nextInt(26)).toChar) mkString "")
  def randomTransfer(): Transfer = Transfer(randomAccount(), randomAccount(), randomCurrency(), rnd.nextLong(1000))


  implicit class SourceExtras[L](val left: Source[L]) extends AnyVal {
    def zip[R](right: Source[R]): Source[(L, R)] = {
      val out = UndefinedSink[(L, R)]
      PartialFlowGraph { implicit b ⇒
        val zip = Zip[L, R]
        left ~> zip.left
        right ~> zip.right
        zip.out ~> out
      }.toSource(out)
    }

    def merge[R >: L](right: Source[R]*): Source[R] =
      Source[R]() { implicit b =>
        val out = UndefinedSink[R]
        val merge = Merge[R]
        left ~> merge
        right.foreach { _ ~> merge }
        merge ~> out
        out
      }

    def mergeX[R >: L](streams: Source[R]*): Source[R] = {
      def rec(level: Int, p: Source[R], s: Seq[Source[R]]): Source[R] =
        if (s.isEmpty) p
        else {
          val toMerge = if (level == 1) s.head else rec(1, s.head, s.tail.take(level - 1))
          rec(level * 2, {
            Source[R]() { implicit b =>
              val out = UndefinedSink[R]
              val merge = Merge[R]
              p ~> merge
              toMerge ~> merge
              merge ~> out
              out
            }
          }, s.drop(level))
        }
      rec(1, left, streams)
    }
  }

  def count(settings: MaterializerSettings, actors: AtomicLong)(implicit system: ActorSystem): FlowMaterializer = {
    import akka.stream.impl.{FlowNameCounter, StreamSupervisor, ActorBasedFlowMaterializer}
    new ActorBasedFlowMaterializer(
      settings,
      system.actorOf(StreamSupervisor.props(settings).withDispatcher(settings.dispatcher)),
      FlowNameCounter(system).counter,
      "flow") {
      override def actorOf(props: Props, name: String): ActorRef = {
        val a = super.actorOf(props, name)
        actors.incrementAndGet()
        a
      }
    }
  }
}