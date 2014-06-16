package preso

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import akka.pattern.after
import scala.concurrent.duration._
import akka.actor.Scheduler

object WebService {
  import Bank._

  val rates = Map(
    Currency("USD") -> 1.3,
    Currency("EUR") -> 1.0,
    Currency("SEK") -> 8.6,
    Currency("PLN") -> 4.1,
    Currency("LTL") -> 3.5,
    Currency("HUF") -> 307.0)
    
  def convertToEUR(currency: Currency, amount: Long)(implicit ec: ExecutionContext): Future[Long] =
    Future((amount / rates(currency)).toLong)

  def convertToEURslow(currency: Currency, amount: Long)(implicit ec: ExecutionContext, sched: Scheduler): Future[Long] =
    after(3.seconds, sched)(Future((amount / rates(currency)).toLong))
    
}