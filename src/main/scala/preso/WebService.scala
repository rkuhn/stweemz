package preso

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import akka.pattern.after
import scala.concurrent.duration._
import akka.actor.Scheduler

object WebService {
  import akka.preso.Bank._

  val rates = Map(
    Currency("USD") -> 1.3,
    Currency("EUR") -> 1.0,
    Currency("SEK") -> 8.6,
    Currency("PLN") -> 4.1,
    Currency("LTL") -> 3.5,
    Currency("HUF") -> 307.0)
    
  def convertToEUR(transfer: Transfer)(implicit ec: ExecutionContext): Future[Transfer] =
    if(transfer.currency.name == "EUR") Future.successful(transfer)
    else Future { transfer.copy(amount = (transfer.amount / rates(transfer.currency)).toLong,
                                currency = Currency("EUR")) }

  def convertToEURslow(transfer: Transfer)(implicit ec: ExecutionContext, sched: Scheduler): Future[Transfer] =
    after(3.seconds, sched)(convertToEUR(transfer))
    
}