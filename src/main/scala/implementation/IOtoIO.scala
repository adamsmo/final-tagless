package implementation

import boilerplate.NaturalTransformation
import cats.effect.IO

//this is only for sake of the demo, so email can be implemented with arbitrary monad
class IOtoIO extends NaturalTransformation[IO, IO] {
  override def transform[A](f: IO[A]): IO[A] = f
}
