package logic

import cats.Monad
import cats.syntax.flatMap._
import cats.syntax.functor._
import domain._
import interfaces.Emails._
import interfaces._

import scala.language.higherKinds

class CustomerEmails[F[_]: Monad: Emails] {
  def sendPurchaseEmail(customer: Customer): F[Seq[EmailType]] =
    if (customer.purchased.values.sum > 1 && customer.purchased.values.sum % 5 == 0) {
      for {
        c <- sendEmail(customer.email, ConfirmationEmail)
        p <- sendEmail(customer.email, PromoCodeEmail)
      } yield Seq(c, p)
    } else {
      sendEmail(customer.email, ConfirmationEmail).map(Seq(_))
    }
}

object CustomerEmails {
  def apply[F[_]](implicit F: CustomerEmails[F]): CustomerEmails[F] = F

  def sendPurchaseEmail[F[_]: CustomerEmails](customer: Customer): F[Seq[EmailType]] = CustomerEmails[F].sendPurchaseEmail(customer)
}

