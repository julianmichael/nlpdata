package nlpdata.datasets.wiki1k

import nlpdata.util._

case class Wiki1kPath(domain: String, suffix: String) {
  def get: String = s"$domain/$suffix.txt"
}
case class Wiki1kSentencePath(filePath: Wiki1kPath, paragraphNum: Int, sentenceNum: Int)

case class Wiki1kFile(
  path: Wiki1kPath,
  id: String,
  revId: String,
  title: String,
  paragraphs: Vector[Vector[Wiki1kSentence]])

case class Wiki1kSentence(path: Wiki1kSentencePath, tokens: Vector[String])
object Wiki1kSentence {
  implicit object Wiki1kSentenceHasTokens extends HasTokens[Wiki1kSentence] {
    override def getTokens(sentence: Wiki1kSentence): Vector[String] = {
      sentence.tokens
    }
  }
}
