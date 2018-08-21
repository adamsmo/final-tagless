package implementation

import boilerplate.Bridge
import cats.effect.IO
import slick.jdbc.H2Profile.api._

class DBIOtoIO(implicit db: DB) extends Bridge[DBIO, IO] {
  override def translate[A](f: DBIO[A]): IO[A] = {
    //todo introduce type information to indicate that this value is from DB
    IO.fromFuture(IO(db.db.run(f.transactionally)))
  }
}
