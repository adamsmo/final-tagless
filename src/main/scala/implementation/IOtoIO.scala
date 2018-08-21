package implementation

import boilerplate.Bridge
import cats.effect.IO

//this is only for sake of the demo, so email can be implemented with arbitrary monad
class IOtoIO extends Bridge[IO, IO] {
  override def translate[A](f: IO[A]): IO[A] = f
}
