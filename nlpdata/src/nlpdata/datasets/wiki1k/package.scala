package nlpdata.datasets

import nlpdata.util._

package object wiki1k {
  val wiki1kDomains = List("wikipedia", "wikinews")

  object Parsing {

    def readFile(path: Wiki1kPath, lines: Iterator[String]): Wiki1kFile = {
      val id = lines.next
      val revId = lines.next
      val title = lines.next
      def makeParagraph(paragraphNum: Int, lines: List[String]) =
        lines.reverse.zipWithIndex.map {
          case (line, index) =>
            Wiki1kSentence(Wiki1kSentencePath(path, paragraphNum, index), line.split(" ").toVector)
        }.toVector
      val paragraphs = {
        val (mostPs, lastParagraphNum, extraLines) =
          lines.foldLeft((List.empty[Vector[Wiki1kSentence]], 0, List.empty[String])) {
            case ((curParagraphs, curParagraphNum, curLines), nextLine) =>
              if (nextLine.isEmpty) {
                val curSentences = makeParagraph(curParagraphNum, curLines)
                (curSentences :: curParagraphs, curParagraphNum + 1, Nil)
              } else {
                (curParagraphs, curParagraphNum, nextLine :: curLines)
              }
          }
        if (extraLines.isEmpty) mostPs.reverse.toVector
        else {
          val newP = makeParagraph(lastParagraphNum, extraLines)
          (newP :: mostPs).reverse.toVector
        }
      }
      Wiki1kFile(path, id, revId, title, paragraphs)
    }
  }
}
