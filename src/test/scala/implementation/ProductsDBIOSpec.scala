package implementation

import domain.Product
import org.scalacheck.Gen
import org.scalacheck.Gen._
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{ Failure, Success }

class ProductsDBIOSpec extends DBIOSpec {

  "A ProductsDBIO" should "find products by name and id" in new Context {
    forAll(productsGen) { product =>
      val findProduct = products.findProductByName(product.name.substring(2))
      val getProduct = products.getProduct(product.id)

      val result: Future[(Seq[Product], Option[Product])] = for {
        //insert data to DB
        _ <- db.run(DB.products += (product.id, product.name, product.amount, product.price))
        //execute test query
        found <- db.run(findProduct)
        byId <- db.run(getProduct)
      } yield (found, byId)

      eventually {
        result.value shouldBe a[Some[_]]
        result.value.map {
          case Success((find, get)) =>
            find.contains(product) shouldBe true
            get shouldBe Some(product)
          case Failure(e) =>
            fail(s"future failed with $e")
        }
      }
    }
  }

  it should "update products" in new Context {
    forAll(productsGen, nameGen, amountGen, priceGen) { (product, newName, newAmount, newPrice) =>
      val newProduct = product.copy(name = newName, amount = newAmount, price = newPrice)
      val updateProduct = products.updateProduct(newProduct)

      val result: Future[(Option[Product], Option[Product])] = for {
        //insert data to DB
        _ <- db.run(DB.products += (product.id, product.name, product.amount, product.price))
        //execute test query
        returned <- db.run(updateProduct)
        //get data from DB
        fromDB <- db.run(DB.products.filter(_.id === product.id).result.headOption)
      } yield {
        val updated = fromDB.map(Product.fromTuple)
        (updated, returned)
      }

      eventually {
        result.value shouldBe a[Some[_]]
        result.value.get match {
          case Success((updated, returned)) =>
            updated shouldBe Some(newProduct)
            returned shouldBe Some(newProduct)
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

    val products = new ProductsDBIO()

    val nameGen: Gen[String] = listOfN(7, alphaChar).map(_.mkString)
    val amountGen: Gen[Long] = choose(0l, Long.MaxValue)
    val priceGen: Gen[Long] = choose(0l, Long.MaxValue)

    val productsGen: Gen[Product] = for {
      name <- nameGen
      amount <- amountGen
      price <- priceGen
      productId <- uuid
    } yield {
      Product(productId, name, amount, price)
    }
  }

}
