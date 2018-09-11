package boilerplate

import scala.language.higherKinds

trait NaturalTransformation[F[_], H[_]] {
  def transform[A](f: F[A]): H[A]
}

object NaturalTransformation {
  def apply[F[_], H[_]](implicit B: NaturalTransformation[F, H]): NaturalTransformation[F, H] = B

  def transform[F[_], H[_], A](f: F[A])(implicit B: NaturalTransformation[F, H]): H[A] = B.transform(f)
}