package logic

import java.util.UUID

import cats.Monad
import cats.syntax.flatMap._
import cats.syntax.functor._
import domain._
import interfaces.Customers._
import interfaces.Products._
import interfaces.{ Customers, Products }

import scala.language.higherKinds

//[F[_]: Monad: CustomerLogic] is just syntactic sugar for (implicit evidence: CustomerLogic[F])
class Purchases[F[_]: Monad: Customers: Products] {
  def purchaseProduct(customerId: UUID, productId: UUID): F[Either[Seq[DomainError], (Customer, Product)]] = {
    for {
      customer <- getCustomer(customerId)
      product <- getProduct(productId).map(_.filter(p => p.amount > 0))
      result <- (customer, product) match {
        case (Some(c), Some(p)) =>
          val amount = c.purchased.get(p.id).map(_ + 1l).getOrElse(1l)
          val updatedCustomer = c.copy(purchased = c.purchased + (p.id -> amount))
          val updatedProduct = p.copy(amount = p.amount - 1)
          persistChanges(updatedCustomer, updatedProduct)

        case (c, p) =>
          //fail means that no customer or product was found
          Monad[F].pure(handleErrors(c, p))
      }
    } yield result
  }

  private def persistChanges(customer: Customer, product: Product) = for {
    c <- updateCustomer(customer)
    p <- updateProduct(product)
  } yield {
    //failure means that customer or product update did not found entity
    handleErrors(c, p)
  }

  private def handleErrors(customer: Option[Customer], product: Option[Product]) = (customer, product) match {
    case (Some(c), Some(p)) => Right((c, p))
    case (Some(_), None)    => Left(Seq(NoProduct))
    case (None, Some(_))    => Left(Seq(NoCustomer))
    case _                  => Left(Seq(NoCustomer, NoProduct))
  }
}

object Purchases {
  def apply[F[_]](implicit F: Purchases[F]): Purchases[F] = F

  def purchaseProduct[F[_]: Purchases](customerId: UUID, productId: UUID): F[Either[Seq[DomainError], (Customer, Product)]] =
    Purchases[F].purchaseProduct(customerId, productId)
}