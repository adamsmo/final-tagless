package implementation

import boilerplate.NaturalTransformation
import cats.effect.IO
import slick.jdbc.H2Profile.api._

class DBIOtoIO(implicit db: DB) extends NaturalTransformation[DBIO, IO] {
  override def transform[A](f: DBIO[A]): IO[A] = {
    //todo introduce type information to indicate that this value is from DB
    IO.fromFuture(IO(db.db.run(f.transactionally)))
  }
}
