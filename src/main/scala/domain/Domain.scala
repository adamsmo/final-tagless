package domain

import java.util.UUID

case class Customer(id: UUID, email: String, purchased: Map[UUID, Long])

//todo add constructor from tuple in slick
case class Product(id: UUID, name: String, amount: Long, price: Long)

sealed trait DomainError

case object NoCustomer extends DomainError

case object NoProduct extends DomainError

sealed trait EmailType

case object ConfirmationEmail extends EmailType

case object PromoCodeEmail extends EmailType

//boiler plate
object Product {
  def fromTuple(t: (UUID, String, Long, Long)) = Product(t._1, t._2, t._3, t._4)
}

object Customer {
  def fromTuple(t: (UUID, String, Map[UUID, Long])) = Customer(t._1, t._2, t._3)
}