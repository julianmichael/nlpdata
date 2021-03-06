package nlpdata.datasets

import nlpdata.structure.SyntaxTree
import nlpdata.structure.SyntaxTreeNode
import nlpdata.structure.SyntaxTreeLeaf
import nlpdata.structure.Word

import scala.util.Try

package object ptb {

  object Parsing {
    import cats._
    import cats.data._
    import cats.implicits._
    private[this] type SentenceState[A] = State[Int, A]

    import fastparse.all._
    private[this] val symbolP: P[String] = P(CharPred(c => !" ()".contains(c)).rep.!)
    // P(CharIn('A' to 'Z', '0' to '9', "-$,.").rep.!)
    private[this] val tokenP: P[String] = P(CharPred(c => !" ()".contains(c)).rep.!)
    private[this] lazy val treeP: P[SentenceState[SyntaxTree]] =
    P("(" ~ symbolP ~ " " ~ treeP.rep ~ ")").map {
      case (symbol, childrenState) =>
        for {
          children <- childrenState.toList.sequence
        } yield SyntaxTreeNode(symbol, children.toList): SyntaxTree
    } | P("(" ~ symbolP ~ " " ~ tokenP ~ ")" ~ " ".?).map {
      case (pos, token) =>
        for {
          index <- State.get
          _     <- State.set(index + 1)
        } yield SyntaxTreeLeaf(Word(index, pos, token)): SyntaxTree
    }
    private[this] val fullTreeP: P[SyntaxTree] =
      P("(" ~ " ".? ~ treeP ~ ")").map(_.runA(0).value)

    /** Parses a SyntaxTree from its flattened column representation in the CoNLL data.
      *
      * Assumes the data is in the correct format. Undefined behavior otherwise.
      *
      * @param s the flattened column representation of the tree
      * @param words the words of the sentence this tree parses
      */
    def readSyntaxTree(s: String): SyntaxTree =
      fullTreeP.parse(s).get.value

    /** Reads a PTBFile from an iterator over lines.
      *
      * Assumes that the given lines are taken directly from a PTB file.
      * Behavior is undefined if not.
      *
      * @param lines the lines of a PTB file
      */
    def readFile(lines: Iterator[String]): PTBFile = {
      val (sentences, lastChunk, lastIndex) = lines
        .foldLeft((List.empty[PTBSentence], List.empty[String], 0)) {
          case ((prevSentences, curLines, sentenceNum), line) =>
            if (line.isEmpty) {
              (prevSentences, curLines, sentenceNum)
            } else if (!line.startsWith(" ") && !curLines.isEmpty) {
              val tree = readSyntaxTree(curLines.reverse.map(_.dropWhile(_ == ' ')).mkString)
              val sentence = PTBSentence(sentenceNum, tree.words, tree)
              (sentence :: prevSentences, line :: Nil, sentenceNum + 1)
            } else {
              (prevSentences, line :: curLines, sentenceNum)
            }
        }
      val lastSentence = {
        val tree = readSyntaxTree(lastChunk.reverse.map(_.dropWhile(_ == ' ')).mkString)
        PTBSentence(lastIndex, tree.words, tree)
      }
      PTBFile((lastSentence :: sentences).toVector.reverse)
    }
  }
}
