package nlpdata.datasets.ptb

import nlpdata.structure.Word
import nlpdata.structure.SyntaxTree

import nlpdata.util.HasTokens

case class PTBFile(sentences: Vector[PTBSentence])

case class PTBSentence(
  sentenceNum: Int,
  words: Vector[Word],
  syntaxTree: SyntaxTree)

object PTBSentence {
  implicit object PTBSentenceHasTokens extends HasTokens[PTBSentence] {
    override def getTokens(sentence: PTBSentence): Vector[String] =
      sentence.words.filter(_.pos != "-NONE-").map(_.token)
  }
}

case class PTBPath(suffix: String)

case class PTBSentencePath(filePath: PTBPath, sentenceNum: Int)
