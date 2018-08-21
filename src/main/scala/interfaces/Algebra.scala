package interfaces

import java.util.UUID

import domain._

import scala.language.higherKinds

trait Customers[F[_]] {
  def updateCustomer(u: Customer): F[Option[Customer]]

  def getCustomer(id: UUID): F[Option[Customer]]
}

trait Products[F[_]] {
  def getProduct(id: UUID): F[Option[Product]]

  def updateProduct(p: Product): F[Option[Product]]

  def findProductByName(name: String): F[Seq[Product]]
}

trait Emails[F[_]] {
  def sendEmail(customer: String, email: EmailType): F[EmailType]
}

//boiler plate
object Customers {
  def apply[F[_]](implicit F: Customers[F]): Customers[F] = F

  def updateCustomer[F[_]: Customers](u: Customer): F[Option[Customer]] = Customers[F].updateCustomer(u)

  def getCustomer[F[_]: Customers](id: UUID): F[Option[Customer]] = Customers[F].getCustomer(id)
}

object Products {
  def apply[F[_]](implicit F: Products[F]): Products[F] = F

  def getProduct[F[_]: Products](id: UUID): F[Option[Product]] = Products[F].getProduct(id)

  def updateProduct[F[_]: Products](p: Product): F[Option[Product]] = Products[F].updateProduct(p)

  def findProductByName[F[_]: Products](name: String): F[Seq[Product]] = Products[F].findProductByName(name)
}

object Emails {
  def apply[F[_]](implicit F: Emails[F]): Emails[F] = F

  def sendEmail[F[_]: Emails](customer: String, email: EmailType): F[EmailType] = Emails[F].sendEmail(customer, email)
}

