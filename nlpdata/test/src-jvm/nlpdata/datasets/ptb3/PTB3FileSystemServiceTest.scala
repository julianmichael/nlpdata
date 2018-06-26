package nlpdata.datasets.ptb3

import cats.implicits._

import org.scalatest._
import org.scalatest.prop._

import scala.util.{Try, Success}

import java.nio.file.Paths

/** Only run this test after you have the PTB downloaded */
class PTB3FileSystemServiceTest extends FunSuite with GeneratorDrivenPropertyChecks with Matchers {

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
}
