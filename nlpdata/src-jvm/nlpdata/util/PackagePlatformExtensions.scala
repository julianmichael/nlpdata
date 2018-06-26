package nlpdata.util

import resource.managed
import resource.ManagedResource

import scala.util.Try

import java.nio.file.{Files, Path, Paths}

trait PackagePlatformExtensions {
  protected[nlpdata] def loadFile(path: Path): ManagedResource[Iterator[String]] = {
    import scala.collection.JavaConverters._
    managed(Files.lines(path)).map(_.iterator.asScala)
  }

  protected[nlpdata] def saveFile(path: Path, contents: String): Try[Unit] =
    Try(Files.write(path, contents.getBytes))
}
