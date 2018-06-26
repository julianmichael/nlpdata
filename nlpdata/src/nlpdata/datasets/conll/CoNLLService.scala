package nlpdata.datasets.conll

import cats.free.Free
import cats.~>
import cats.Monad
import cats.implicits._

trait CoNLLService[M[_]] {

  protected implicit def monad: Monad[M]

  def getFile(path: CoNLLPath): M[CoNLLFile]

  def getAllPaths: M[List[CoNLLPath]]

  def getSentence(path: CoNLLSentencePath): M[CoNLLSentence] =
    getFile(path.filePath).map(_.sentences(path.sentenceNum))

  def getAllSentencePaths: M[List[CoNLLSentencePath]] =
    for {
      paths <- getAllPaths
      files <- paths.map(getFile).sequence
    } yield files.flatMap(_.sentences.map(_.path))

  final def interpreter: (CoNLLServiceRequestA ~> M) =
    new (CoNLLServiceRequestA ~> M) {
      import CoNLLServiceRequestA._

      def apply[A](op: CoNLLServiceRequestA[A]): M[A] = op match {
        case GetFile(path)             => getFile(path)
        case GetAllPaths               => getAllPaths
        case GetSentence(sentencePath) => getSentence(sentencePath)
        case GetAllSentencePaths       => getAllSentencePaths
      }
    }

  final def interpretThrough[G[_]: Monad](transform: M ~> G): CoNLLService[G] =
    new CoNLLService.CompoundCoNLLService(this, transform)
}

object CoNLLService {
  private class CompoundCoNLLService[M[_], G[_]](base: CoNLLService[M], transform: M ~> G)(
    implicit M: Monad[M],
    G: Monad[G]
  ) extends CoNLLService[G] {
    override protected implicit val monad = G

    def getFile(path: CoNLLPath): G[CoNLLFile] =
      transform(base.getFile(path))

    def getAllPaths: G[List[CoNLLPath]] =
      transform(base.getAllPaths)

    override def getSentence(path: CoNLLSentencePath): G[CoNLLSentence] =
      transform(base.getSentence(path))

    override def getAllSentencePaths: G[List[CoNLLSentencePath]] =
      transform(base.getAllSentencePaths)
  }
}

sealed trait CoNLLServiceRequestA[A]

object CoNLLServiceRequestA {
  case class GetFile(path: CoNLLPath) extends CoNLLServiceRequestA[CoNLLFile]
  case object GetAllPaths extends CoNLLServiceRequestA[List[CoNLLPath]]
  case class GetSentence(sentencePath: CoNLLSentencePath)
      extends CoNLLServiceRequestA[CoNLLSentence]
  case object GetAllSentencePaths extends CoNLLServiceRequestA[List[CoNLLSentencePath]]
}

object FreeCoNLLService extends CoNLLService[Free[CoNLLServiceRequestA, ?]] {

  type CoNLLServiceRequest[A] = Free[CoNLLServiceRequestA, A]

  protected implicit override val monad: Monad[CoNLLServiceRequest] =
    implicitly[Monad[CoNLLServiceRequest]]

  def getFile(path: CoNLLPath): CoNLLServiceRequest[CoNLLFile] =
    Free.liftF[CoNLLServiceRequestA, CoNLLFile](CoNLLServiceRequestA.GetFile(path))

  def getAllPaths: CoNLLServiceRequest[List[CoNLLPath]] =
    Free.liftF[CoNLLServiceRequestA, List[CoNLLPath]](CoNLLServiceRequestA.GetAllPaths)

  override def getSentence(sentencePath: CoNLLSentencePath): CoNLLServiceRequest[CoNLLSentence] =
    Free.liftF[CoNLLServiceRequestA, CoNLLSentence](CoNLLServiceRequestA.GetSentence(sentencePath))

  override def getAllSentencePaths: CoNLLServiceRequest[List[CoNLLSentencePath]] =
    Free.liftF[CoNLLServiceRequestA, List[CoNLLSentencePath]](
      CoNLLServiceRequestA.GetAllSentencePaths
    )
}
