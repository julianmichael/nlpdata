package nlpdata.datasets.ptb3

import nlpdata.structure._

// only supports wsj and brown right now

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
