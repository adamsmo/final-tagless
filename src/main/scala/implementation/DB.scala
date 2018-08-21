package implementation

import java.util.UUID

import slick.jdbc.H2Profile.api._
import slick.lifted.{ ForeignKeyQuery, PrimaryKey, ProvenShape }

import scala.concurrent.Future
import scala.language.higherKinds

object DB {

  class Customer(tag: Tag) extends Table[(UUID, String)](tag, "CUSTOMERS") {
    def id: Rep[UUID] = column[UUID]("CUSTOMER_ID", O.PrimaryKey)

    def email: Rep[String] = column[String]("EMAIL")

    def * : ProvenShape[(UUID, String)] = (id, email)
  }

  val customers = TableQuery[Customer]

  class Product(tag: Tag) extends Table[(UUID, String, Long, Long)](tag, "PRODUCTS") {
    def id: Rep[UUID] = column[UUID]("PRODUCT_ID", O.PrimaryKey)

    def name: Rep[String] = column[String]("NAME")

    def amount: Rep[Long] = column[Long]("AMOUNT")

    def price: Rep[Long] = column[Long]("PRICE")

    def * : ProvenShape[(UUID, String, Long, Long)] = (id, name, amount, price)
  }

  val products = TableQuery[Product]

  class Purchase(tag: Tag) extends Table[(UUID, UUID, Long)](tag, "PURCHASES") {
    def pk: PrimaryKey = primaryKey("PURCHASE_PK", (customerId, productId))

    def customerId: Rep[UUID] = column[UUID]("CUSTOMER_ID")

    def productId: Rep[UUID] = column[UUID]("PRODUCT_ID")

    def amount: Rep[Long] = column[Long]("AMOUNT")

    def * : ProvenShape[(UUID, UUID, Long)] = (customerId, productId, amount)

    def cFK: ForeignKeyQuery[Customer, (UUID, String)] = foreignKey("CUSTOMER_FK", customerId, customers)(_.id)

    def pFK: ForeignKeyQuery[Product, (UUID, String, Long, Long)] = foreignKey("PRODUCT_FK", productId, products)(_.id)
  }

  val purchases = TableQuery[Purchase]
}

class DB {

  import DB._

  //todo rename it
  val db = Database.forConfig("h2mem1")

  def setUpDB(): Future[Unit] = {
    val action = DBIO.seq(
      // Create the tables
      customers.schema.create,
      products.schema.create,
      purchases.schema.create,
      // fill the tables
      customers ++= Seq(
        (UUID.fromString("d5872805-8f8d-4863-b827-558d55fb1428"), "aaa@z.y"),
        (UUID.randomUUID(), "bbb@z.y"),
        (UUID.randomUUID(), "ccc@z.y")),
      products ++= Seq(
        (UUID.fromString("f8670917-b709-40ff-8254-6f5b34caa4c8"), "tea", 10, 799),
        (UUID.randomUUID(), "coffee", 49, 899),
        (UUID.randomUUID(), "water", 150, 999)))

    db.run(action)
  }
}