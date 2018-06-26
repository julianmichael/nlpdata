package nlpdata.datasets.conll

import nlpdata.datasets.propbank
import nlpdata.structure._

import scala.util.Try

import cats.data.State
import cats.data.NonEmptyList
import cats.implicits._

object CoNLLParsing {
  private[this] type SentenceState[A] = State[List[Word], A]

  import fastparse.all._
  private[this] val symbolP: P[String] = P(CharIn('A' to 'Z').rep.!)
  private[this] lazy val treeP: P[SentenceState[SyntaxTree]] =
    P("(" ~ symbolP ~ treeP.rep ~ ")").map {
      case (symbol, childrenState) => for {
        children <- childrenState.toList.sequence
      } yield SyntaxTreeNode(symbol, children.toList): SyntaxTree
    } | P("*").map { _ =>
      for {
        words <- State.get
        _ <- State.set(words.tail)
      } yield SyntaxTreeLeaf(words.head)
    }

  /** Parses a SyntaxTree from its flattened column representation in the CoNLL data.
    *
    * Assumes the data is in the correct format. Undefined behavior otherwise.
    *
    * @param s the flattened column representation of the tree
    * @param words the words of the sentence this tree parses
    */
  def readSyntaxTree(s: String, words: List[Word]): SyntaxTree =
    treeP.parse(s).get.value.runA(words).value

  /** Reads a CoNLLSentence from a list of lines from a CoNLLFile.
    * This will grow as the number of fields of CoNLLSentence grows.
    *
    * Does not expect empty lines on either end of the list.
    * Assumes the lines are taken from a CoNLL data file,
    * undefined behavior otherwise.
    *
    * @param sentenceNum the index of the sentence in the document
    * @param lines the lines of the file containing the sentence's info
    * @return the CoNLL sentence stored in the data
    */
  def readSentence(path: CoNLLPath, sentenceNum: Int, lines: NonEmptyList[String]): CoNLLSentence = {
    val lineArrays = lines.map(_.split("\\s+"))
    val partNum = lineArrays.head(1).toInt
    val words = lineArrays.map(arr => Word(arr(2).toInt, arr(4), arr(3))).toList
    val treeString = lineArrays.map(arr => arr(5)).fold
    val tree = readSyntaxTree(treeString, words)
    val predicates = for {
      (arr, index) <- lineArrays.zipWithIndex.toList
      predicateLemma = arr(6)
      if !predicateLemma.equals("-")
      framesetId = arr(7)
      if !framesetId.equals("-")
      head = words(index)
    } yield Predicate(head, predicateLemma, framesetId)
    val paStructures = for {
      (pred, num) <- predicates.zipWithIndex // num of predicate tells us which col the args are in
      spansString = lineArrays.map(arr => arr(11 + num)).fold
      argSpans = propbank.Parsing.readArgumentSpans(spansString, words)
    } yield PredicateArgumentStructure(pred, argSpans)
    val sentencePath = CoNLLSentencePath(path, sentenceNum)
    CoNLLSentence(sentencePath, partNum, words, tree, paStructures)
  }

  private[this] val firstLineRegex = """#begin document \((.*)\); part ([0-9]+)""".r
  private[this] val endDocumentLine = "#end document"

  /** Reads a CoNLLFile from an iterator over lines.
    *
    * Assumes that the given lines are taken directly from a CoNLL file.
    * Behavior is undefined if not.
    * See http://conll.cemantix.org/2012/data.html for the data format.
    *
    * @param lines the lines of a CoNLL file
    */
  def readFile(path: CoNLLPath, lines: Iterator[String]): CoNLLFile = {
    val (sentences, _, _) = lines.foldLeft((List.empty[CoNLLSentence], List.empty[String], 0)) {
      case (acc @ (prevSentences, curLines, sentenceNum), line) =>
        if(line.trim.isEmpty) NonEmptyList.fromList(curLines).fold(acc) { neCurLines =>
          (readSentence(path, sentenceNum, neCurLines.reverse) :: prevSentences, Nil, sentenceNum + 1)
        } else if(firstLineRegex.findFirstIn(line).nonEmpty || line.equals(endDocumentLine)) {
          acc
        } else {
          (prevSentences, line :: curLines, sentenceNum)
        }
    }
    CoNLLFile(path, sentences.toVector.reverse)
  }
}
