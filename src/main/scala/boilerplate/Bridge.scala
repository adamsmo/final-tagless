package boilerplate

import scala.language.higherKinds

trait Bridge[F[_], H[_]] {
  def translate[A](f: F[A]): H[A]
}

object Bridge {
  def apply[F[_], H[_]](implicit B: Bridge[F, H]): Bridge[F, H] = B

  def translate[F[_], H[_], A](f: F[A])(implicit B: Bridge[F, H]): H[A] = B.translate(f)
}