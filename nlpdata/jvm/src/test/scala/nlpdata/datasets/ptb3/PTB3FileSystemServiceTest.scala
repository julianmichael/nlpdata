package nlpdata.datasets.ptb3

import cats.implicits._

// import org.typelevel.discipline.scalatest._
import org.scalatest._
import org.scalatest.prop._

import scala.util.{Try, Success}

import java.nio.file.Paths

/** Only run this test after you have the PTB downloaded */
class PTB3FileSystemServiceTest extends FunSuite /*with Discipline */with GeneratorDrivenPropertyChecks with Matchers {

  import org.scalatest.Inside._
  import org.scalatest.AppendedClues._

  // need to download the data to this location first
  val service = new PTB3FileSystemService(Paths.get("data/ptb3"))

  lazy val pathsTry = service.getAllPaths

  test("Finds some paths") {
    inside(pathsTry) {
      case Success(paths) => paths.size should be > 0
    }
  }

  // for simplicity
  lazy val paths = pathsTry.get

  def pathParsesCorrectly(path: PTB3Path) = {
    inside(service.getFile(path)) {
      case Success(file) =>
        file.path shouldBe path
        file.sentences should not be empty
    } withClue (s"when parsing file at $path")
  }

  test("First file can be parsed into nonempty files with correct paths") {
    pathParsesCorrectly(paths.head)
  }

  test("All paths can be parsed into nonempty files with correct paths") {
    paths.foreach(pathParsesCorrectly)
  }

  // lazy val wsjPaths = paths.collect { case p @ WSJPath(_, _) => p }
  // lazy val wsjFiles = wsjPaths.map(service.getFile(_)).sequence.get
  // lazy val wsjSentences = wsjFiles.flatMap(_.sentences)

  // println(s"WSJ paths: ${wsjPaths.size}")
  // println(s"WSJ sentences: ${wsjSentences.size}")
  // println(s"WSJ words: ${wsjSentences.map(_.syntaxTree.words.size).sum}")
}
