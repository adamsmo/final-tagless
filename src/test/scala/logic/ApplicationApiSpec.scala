package logic

import java.util.UUID

import boilerplate.Bridge
import domain._
import interfaces._
import logic.AlgebraTest._
import org.scalatest.{ FlatSpec, Matchers }

class ApplicationApiSpec extends FlatSpec with Matchers {
  "A ApplicationApi" should "allow to purchase available product" in new Context {
    val state = TestState(
      Map(productId -> product),
      Map(customerId -> customer))

    val purchase: StateMonad[Either[Seq[DomainError], Seq[EmailType]]] = api.makePurchase(customerId, productId)
    val (finalState, executionResult) = purchase.run(state).value

    //check execution results
    executionResult shouldBe Right(Seq(ConfirmationEmail))

    //check DB
    finalState.customers.size shouldBe 1
    finalState.customers.get(customerId) shouldBe Some(customer.copy(purchased = Map(productId -> 1)))
    finalState.products.size shouldBe 1
    finalState.products.get(productId) shouldBe Some(product.copy(amount = productAmount - 1))
  }

  it should "send promo email every 5`th product" in new Context {
    val state = TestState(
      Map(productId -> product),
      Map(customerId -> customer.copy(purchased = Map(productId -> 4))))

    val purchase: StateMonad[Either[Seq[DomainError], Seq[EmailType]]] = api.makePurchase(customerId, productId)
    val (finalState, executionResult) = purchase.run(state).value

    //check execution results
    executionResult shouldBe Right(Seq(ConfirmationEmail, PromoCodeEmail))

    //check DB
    finalState.customers.size shouldBe 1
    finalState.customers.get(customerId) shouldBe Some(customer.copy(purchased = Map(productId -> 5)))
    finalState.products.size shouldBe 1
    finalState.products.get(productId) shouldBe Some(product.copy(amount = productAmount - 1))
  }

  it should "fail to purchase non existing product" in new Context {
    val state = TestState(
      Map.empty,
      Map(customerId -> customer.copy(purchased = Map(productId -> 4))))

    val purchase: StateMonad[Either[Seq[DomainError], Seq[EmailType]]] = api.makePurchase(customerId, productId)
    val (finalState, executionResult) = purchase.run(state).value

    //check execution results
    executionResult shouldBe Left(List(NoProduct))

    //check DB
    finalState shouldBe state
  }

  it should "fail to purchase non sold out product" in new Context {
    val state = TestState(
      Map(productId -> product.copy(amount = 0)),
      Map(customerId -> customer.copy(purchased = Map(productId -> 4))))

    val purchase: StateMonad[Either[Seq[DomainError], Seq[EmailType]]] = api.makePurchase(customerId, productId)
    val (finalState, executionResult) = purchase.run(state).value

    //check execution results
    executionResult shouldBe Left(List(NoProduct))

    //check DB
    finalState shouldBe state
  }

  it should "fail to purchase product if customer does not exists" in new Context {
    val state = TestState(
      Map(productId -> product),
      Map.empty)

    val purchase: StateMonad[Either[Seq[DomainError], Seq[EmailType]]] = api.makePurchase(customerId, productId)
    val (finalState, executionResult) = purchase.run(state).value

    //check execution results
    executionResult shouldBe Left(List(NoCustomer))

    //check DB
    finalState shouldBe state
  }

  it should "find existing products" in new Context {
    val state = TestState(
      Map(productId -> product),
      Map(customerId -> customer))

    val found: StateMonad[Either[Seq[DomainError], Seq[Product]]] = api.findProduct(customerId, product.name.substring(2, 4))
    val (finalState, executionResult) = found.run(state).value

    //check execution results
    executionResult shouldBe Right(List(product))

    //check DB
    finalState shouldBe state
  }

  it should "do not find product by invalid name" in new Context {
    val state = TestState(
      Map(productId -> product),
      Map(customerId -> customer))

    val found: StateMonad[Either[Seq[DomainError], Seq[Product]]] = api.findProduct(customerId, "no such product")
    val (finalState, executionResult) = found.run(state).value

    //check execution results
    executionResult shouldBe Right(List.empty)

    //check DB
    finalState shouldBe state
  }

  it should "return search error when customer does not exists" in new Context {
    val state = TestState(
      Map(productId -> product),
      Map.empty)

    val found: StateMonad[Either[Seq[DomainError], Seq[Product]]] = api.findProduct(customerId, "no such product")
    val (finalState, executionResult) = found.run(state).value

    //check execution results
    executionResult shouldBe Left(List(NoCustomer))

    //check DB
    finalState shouldBe state
  }

  trait Context {
    val customerId: customerId = UUID.randomUUID()
    val productId: productId = UUID.randomUUID()
    val productAmount = 42
    val productPrice = 399
    val customer = Customer(customerId, "a@b.c", Map.empty)
    val product = Product(productId, "best product", productAmount, productPrice)

    implicit val customers: Customers[StateMonad] = new CustomersState
    implicit val products: Products[StateMonad] = new ProductsState
    implicit val emails: Emails[StateMonad] = new EmailsState
    implicit val bridge: Bridge[StateMonad, StateMonad] = new BridgeState

    implicit val purchases: Purchases[StateMonad] = new Purchases[StateMonad]
    implicit val customerEmails: CustomerEmails[StateMonad] = new CustomerEmails[StateMonad]

    val api = new ApplicationApi[StateMonad, StateMonad]
  }

}
