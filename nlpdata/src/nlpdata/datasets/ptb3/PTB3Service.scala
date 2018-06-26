package nlpdata.datasets.ptb3

import cats._
import cats.implicits._
import cats.free.Free

import nlpdata.structure.SyntaxTree

// only supports parse trees right now

// can extend directly, in order to override convenience methods for efficiency
// or can create using an interpreter.

// TODO: maybe there is a nice way of designing this to make it easier to have:
// items in the AST data type that represent ALL of the operations,
// including those implementable on the basis of the others,
// while making it easy to use the default implementations in interpreters if desired.

trait PTB3Service[M[_]] {

  implicit protected val monad: Monad[M]

  def getFile(path: PTB3Path): M[PTB3File]

  def getAllPaths: M[List[PTB3Path]]

  def getAllSentencePaths: M[List[PTB3SentencePath]] =
    for {
      paths <- getAllPaths
      files <- paths.map(getFile).sequence
    } yield files.flatMap(file => file.sentences.map(_.path))

  def getSentence(path: PTB3SentencePath): M[PTB3Sentence] =
    getFile(path.filepath).map(_.sentences(path.sentenceNum))

  // maybe for ease of migration

  def getParseTree(path: PTB3SentencePath): M[SyntaxTree] =
    getSentence(path).map(_.syntaxTree)
}

sealed trait PTB3ServiceRequestA[A]
case class GetFile(path: PTB3Path) extends PTB3ServiceRequestA[PTB3File]
case object GetAllPaths extends PTB3ServiceRequestA[List[PTB3Path]]

object FreePTB3Service extends PTB3Service[Free[PTB3ServiceRequestA, ?]] {

  type PTB3ServiceRequest[A] = Free[PTB3ServiceRequestA, A]

  protected override val monad: Monad[PTB3ServiceRequest] =
    implicitly[Monad[PTB3ServiceRequest]]

  def getFile(path: PTB3Path): PTB3ServiceRequest[PTB3File] =
    Free.liftF[PTB3ServiceRequestA, PTB3File](GetFile(path))

  def getAllPaths: PTB3ServiceRequest[List[PTB3Path]] =
    Free.liftF[PTB3ServiceRequestA, List[PTB3Path]](GetAllPaths)
}

class InterpretedPTB3Service[M[_]](interpreter: PTB3ServiceRequestA ~> M)(
  protected override implicit val monad: Monad[M]
) extends PTB3Service[M] {

  override def getFile(path: PTB3Path): M[PTB3File] =
    FreePTB3Service.getFile(path).foldMap(interpreter)

  override def getAllPaths: M[List[PTB3Path]] =
    FreePTB3Service.getAllPaths.foldMap(interpreter)
}
