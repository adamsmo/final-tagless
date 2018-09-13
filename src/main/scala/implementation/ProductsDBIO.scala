package implementation

import java.util.UUID

import domain.Product
import interfaces.Products
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext

class ProductsDBIO(implicit ec: ExecutionContext) extends Products[DBIO] {
  override def getProduct(id: UUID): DBIO[Option[Product]] =
    DB.products
      .filter(_.id === id)
      .result
      .headOption
      .map(_.map { case (_, name, amount, price) => Product(id, name, amount, price) })

  override def updateProduct(p: Product): DBIO[Option[Product]] =
    DB.products.filter(_.id === p.id).update(p.id, p.name, p.amount, p.price).map {
      case 0 => None
      case _ => Some(p)
    }

  override def findProductByName(nameFragment: String): DBIO[Seq[Product]] = {
    DB.products.filter(_.name like s"%$nameFragment%").result.map(_.map {
      case (id, name, amount, price) =>
        Product(id, name, amount, price)
    })
  }
}