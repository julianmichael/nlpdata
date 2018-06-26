package nlpdata.datasets.conll

import cats.~>
import cats.Monad
import cats.implicits._

import nlpdata.util._

import scala.util.{Failure, Success, Try}
import java.nio.file.{Files, Path, Paths}

class CoNLLFileSystemService(location: Path) extends CoNLLService[Try] {

  protected implicit override val monad = implicitly[Monad[Try]]

  import com.softwaremill.macmemo.memoize
  import com.softwaremill.macmemo.MemoCacheBuilder
  private[this] implicit val cacheProvider =
    MemoCacheBuilder.guavaMemoCacheBuilder
  import scala.concurrent.duration._

  @memoize(maxSize = 200, expiresAfter = 1.hour)
  private[this] def getFileUnsafe(path: CoNLLPath): CoNLLFile = {
    loadFile(location.resolve(path.suffix))
      .map(lines => CoNLLParsing.readFile(path, lines))
      .tried
      .get
  }

  def getFile(path: CoNLLPath): Try[CoNLLFile] =
    Try(getFileUnsafe(path))

  def getAllPaths: Try[List[CoNLLPath]] = Try {
    def listFilePathsInRecursiveSubdirectories(rootPath: Path, file: java.io.File): List[String] =
      if (!file.isDirectory) List(rootPath.resolve(file.getName).toString)
      else
        file.listFiles.toList
          .flatMap(listFilePathsInRecursiveSubdirectories(rootPath.resolve(file.getName), _))
    new java.io.File(location.toString).listFiles.toList
      .flatMap(listFilePathsInRecursiveSubdirectories(Paths.get(""), _))
      .flatMap(CoNLLPath.fromPathSuffix)
  }

  // TODO could implement caching of sentence paths for efficiency
}
