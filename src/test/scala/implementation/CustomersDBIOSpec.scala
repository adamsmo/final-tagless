package implementation

import domain.Customer
import org.scalacheck.Gen
import org.scalacheck.Gen.{ alphaChar, choose, listOfN, uuid }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{ Failure, Success }

class CustomersDBIOSpec extends DBIOSpec {
  "A CustomersDBIO" should "find customers" in new Context {
    //TODO add tests for customers logic
  }

  it should "not update customer if it does not exists" in new Context {
    forAll(customersGen) { customer =>
      val customerUpdate = customers.updateCustomer(customer)

      //todo add DB check
      val result: Future[Option[Customer]] = db.run(customerUpdate)

      eventually {
        result.value shouldBe a[Some[_]]
        result.value.get match {
          case Success(updated) =>
            updated shouldBe None
          case Failure(e) =>
            fail(s"future failed with $e")
        }
      }
    }
  }

  trait Context {
    implicit val database: DB = new DB()
    val db = database.db

    val setup: Future[Unit] = database.setUpDB()
    eventually {
      setup.isCompleted shouldBe true
    }

    val customers = new CustomersDBIO()

    val emailGen: Gen[String] = for {
      account <- listOfN(7, alphaChar)
      site <- listOfN(5, alphaChar)
      domain <- listOfN(2, alphaChar)
    } yield {
      ((account :+ '@') ++ site ++ ('.' +: domain)).mkString
    }
    val amountGen: Gen[Long] = choose(0l, Long.MaxValue)

    val customersGen: Gen[Customer] = for {
      customerId <- uuid
      email <- emailGen
    } yield {
      Customer(customerId, email, Map.empty)
    }
  }

}
