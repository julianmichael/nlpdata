package nlpdata.datasets.ptb3

import nlpdata.structure._

// only supports wsj and brown right now

object PTB3Path {

  private[this] val WSJPathRegex = """([0-9]{2})/WSJ_[0-9]{2}([0-9]{2})\.MRG""".r
  private[this] object IntMatch {
    def unapply(s: String): Option[Int] = scala.util.Try(s.toInt).toOption
  }

  def fromPTBPath(path: nlpdata.datasets.ptb.PTBPath) = path.suffix match {
    case WSJPathRegex(IntMatch(section), IntMatch(number)) => Some(WSJPath(section, number))
    case _ => None
  }
}

object PTB3SentencePath {
  def fromPTBSentencePath(
    sentencePath: nlpdata.datasets.ptb.PTBSentencePath): Option[PTB3SentencePath] = sentencePath match {
    case nlpdata.datasets.ptb.PTBSentencePath(path, sentenceNum) =>
      PTB3Path.fromPTBPath(path).map(PTB3SentencePath(_, sentenceNum))
  }
}

sealed trait PTB3Path

case class WSJPath(
  section: Int, // 0-24 inclusive
  number: Int // 0-99, doesn't include all numbers
) extends PTB3Path

case class BrownPath(
  domain: String,
  number: Int // 0-99 inclusive; doesn't include all numbers
) extends PTB3Path

case class PTB3SentencePath(
  filepath: PTB3Path,
  sentenceNum: Int)

case class PTB3File(
  path: PTB3Path,
  sentences: Vector[PTB3Sentence])

case class PTB3Sentence(
  path: PTB3SentencePath,
  words: Vector[Word],
  syntaxTree: SyntaxTree)
