package nlpdata.datasets.qasrl

import cats.Monad
import cats.syntax.all._

trait QASRLService[M[_]] {
  implicit protected def monad: Monad[M]

  // TODO define general service API
}
