package boilerplate

import cats.Monad
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext

class MonadDBIO(implicit ec: ExecutionContext) extends Monad[DBIO] {
  override def pure[A](x: A): DBIO[A] = DBIO.successful(x)

  override def flatMap[A, B](fa: DBIO[A])(f: A => DBIO[B]): DBIO[B] = fa.flatMap(f)

  override def tailRecM[A, B](a: A)(f: A => DBIO[Either[A, B]]): DBIO[B] = f(a).flatMap {
    case Left(next) => tailRecM(next)(f)
    case Right(b)   => DBIO.successful(b)
  }
}