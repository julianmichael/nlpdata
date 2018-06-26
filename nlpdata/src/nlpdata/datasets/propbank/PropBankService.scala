package nlpdata.datasets.propbank

import cats.Monad
import cats.syntax.all._

trait PropBankService[M[_]] {
  implicit protected def monad: Monad[M]

  def getFile(path: PropBankPath): M[PropBankFile]

  def getSentence(path: PropBankSentencePath): M[PropBankSentence] =
    getFile(path.filePath).map(_.sentences(path.sentenceNum))
}
