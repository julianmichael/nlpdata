package nlpdata.datasets

import nlpdata.structure._
import nlpdata.util._

import scala.util.Try

package object ptb3 {
  implicit object PTB3SentenceHasTokens extends HasTokens[PTB3Sentence] {
    override def getTokens(sentence: PTB3Sentence): Vector[String] =
      sentence.words.filter(_.pos != "-NONE-").map(_.token)
  }

  object Parsing {
    import cats._
    import cats.data._
    import cats.implicits._
    private[this] type SentenceState[A] = State[Int, A]

    import fastparse.all._
    private[this] val symbolP: P[String] = P(CharPred(c =>  !" ()".contains(c)).rep.!)
    // P(CharIn('A' to 'Z', '0' to '9', "-$,.").rep.!)
    private[this] val tokenP: P[String] = P(CharPred(c =>  !" ()".contains(c)).rep.!)
    private[this] lazy val treeP: P[SentenceState[SyntaxTree]] =
      P("(" ~ symbolP ~ " " ~ treeP.rep ~ ")").map {
        case (symbol, childrenState) => for {
          children <- childrenState.toList.sequence
        } yield SyntaxTreeNode(symbol, children.toList): SyntaxTree
      } | P("(" ~ symbolP ~ " " ~ tokenP ~ ")" ~ " ".?).map {
        case (pos, token) => for {
          index <- State.get
          _ <- State.set(index + 1)
        } yield SyntaxTreeLeaf(Word(index, pos, token)): SyntaxTree
      }
    // This is not what the data is SUPPOSED to look like, but it does in the brown corpus because horribleness.
    // so we add a TOP node to every tree to hold all of the top-level labeled trees in each example.
    private[this] val allTreesP: P[List[SyntaxTree]] =
      P("(" ~ " ".? ~ treeP.rep(1) ~ ")").map(_.toList.sequence.runA(0).value)
    private[this] val fullTreeP: P[SyntaxTree] =
      allTreesP.map(SyntaxTreeNode("TOP", _))

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
    def readFile(path: PTB3Path, lines: Iterator[String]): PTB3File = {
      val (sentences, lastChunk, lastIndex) = lines
        .dropWhile(_.startsWith("*")) // to get rid of copyright comments in Brown corpus
        .foldLeft((List.empty[PTB3Sentence], List.empty[String], 0)) {
        case ((prevSentences, curLines, sentenceNum), line) =>
          if(line.isEmpty) {
            (prevSentences, curLines, sentenceNum)
          } else if(!line.startsWith(" ") && !curLines.isEmpty) {
            val tree = readSyntaxTree(curLines.reverse.map(_.dropWhile(_ == ' ')).mkString)
            val sentence = PTB3Sentence(PTB3SentencePath(path, sentenceNum), tree.words, tree)
            (sentence :: prevSentences, line :: Nil, sentenceNum + 1)
          } else {
            (prevSentences, line :: curLines, sentenceNum)
          }
      }
      val lastSentence = {
        val tree = readSyntaxTree(lastChunk.reverse.map(_.dropWhile(_ == ' ')).mkString)
        PTB3Sentence(PTB3SentencePath(path, lastIndex), tree.words, tree)
      }
      PTB3File(path, (lastSentence :: sentences).toVector.reverse)
    }
  }
}
