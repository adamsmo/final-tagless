package implementation

import java.util.UUID

import domain._
import interfaces._
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext

class CustomersDBIO(implicit db: DB, ec: ExecutionContext) extends Customers[DBIO] {
  override def updateCustomer(c: Customer): DBIO[Option[Customer]] =
    (for {
      updateCount <- DB.customers.filter(_.id === c.id).update(c.id, c.email)
      toReturn <- updateCount match {
        case 0 =>
          DBIO.successful(None)
        case _ =>
          DBIO.seq(c.purchased.toSeq.map { case (pId, amount) => DB.purchases.insertOrUpdate(c.id, pId, amount) }: _*)
            .map(_ => Some(c))
      }
    } yield toReturn).transactionally

  override def getCustomer(id: UUID): DBIO[Option[Customer]] =
    (for {
      c <- DB.customers.filter(_.id === id).result.headOption
      ps <- DB.purchases.filter(_.customerId === id).result
    } yield {
      val products = ps.foldLeft(Map.empty[UUID, Long]) {
        case (map, (_, pId, amount)) => map + (pId -> amount)
      }
      c.map { case (_, email) => Customer(id, email, products) }
    }).transactionally
}

