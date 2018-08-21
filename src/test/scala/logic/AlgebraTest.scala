package logic

import java.util.UUID

import boilerplate._
import cats.data.State
import domain.{ Customer, EmailType, Product }
import interfaces._

import scala.language.higherKinds

object AlgebraTest {

  type customerId = UUID
  type productId = UUID

  case class TestState(
      products: Map[productId, Product],
      customers: Map[customerId, Customer])

  type StateMonad[A] = State[TestState, A]

  class CustomersState extends Customers[StateMonad] {
    def updateCustomer(c: Customer): State[TestState, Option[Customer]] =
      State { state =>
        state.customers.get(c.id) match {
          case Some(_) =>
            val nextCustomers = state.customers + (c.id -> c)
            (state.copy(customers = nextCustomers), Some(c))
          case None =>
            (state, None)
        }
      }

    def getCustomer(id: UUID): State[TestState, Option[Customer]] =
      State { state =>
        val c = state.customers.get(id)
        (state, c)
      }
  }

  class ProductsState extends Products[StateMonad] {
    def getProduct(id: UUID): State[TestState, Option[Product]] =
      State { state =>
        val p = state.products.get(id)
        (state, p)
      }

    def updateProduct(p: Product): State[TestState, Option[Product]] =
      State { state =>
        state.products.get(p.id) match {
          case Some(_) =>
            val nextProducts = state.products + (p.id -> p)
            (state.copy(products = nextProducts), Some(p))
          case None =>
            (state, None)
        }
      }

    def findProductByName(name: String): State[TestState, Seq[Product]] =
      State { state =>
        val products = state.products.values.filter(_.name.contains(name)).toSeq
        (state, products)
      }
  }

  class EmailsState extends Emails[StateMonad] {
    def sendEmail(customer: String, email: EmailType): State[TestState, EmailType] = State(state => (state, email))
  }

  class BridgeState extends Bridge[StateMonad, StateMonad] {
    def translate[A](f: State[TestState, A]): State[TestState, A] = f
  }
}

