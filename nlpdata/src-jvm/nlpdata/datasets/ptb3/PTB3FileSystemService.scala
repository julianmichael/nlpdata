package nlpdata.datasets.ptb3

import cats._
import cats.arrow.FunctionK
import cats.implicits._

import nlpdata.util._

import scala.util.Try
import scala.language.implicitConversions

import java.nio.file.{Files, Path, Paths}

class PTB3FileSystemInterpreter(location: Path) extends (PTB3ServiceRequestA ~> Try) {

  def apply[A](request: PTB3ServiceRequestA[A]): Try[A] = request match {
    case GetFile(path) => getFile(path)
    case GetAllPaths   => allPTBPaths
  }

  private[this] val annotationPath =
    location.resolve(Paths.get("TREEBANK_3/PARSED/MRG"))

  import com.softwaremill.macmemo.memoize
  import com.softwaremill.macmemo.MemoCacheBuilder
  private[this] implicit val cacheProvider =
    MemoCacheBuilder.guavaMemoCacheBuilder
  import scala.concurrent.duration._
  import scala.language.postfixOps

  @memoize(maxSize = 1000, expiresAfter = 1 hour)
  private[this] def getFileUnsafe(path: PTB3Path): PTB3File = {
    val fullPath = path match {
      case WSJPath(section, number) =>
        annotationPath.resolve(f"WSJ/${section}%02d/WSJ_${section}%02d${number}%02d.MRG")
      case BrownPath(domain, number) =>
        annotationPath.resolve(f"BROWN/$domain/${domain}${number}%02d.MRG")
    }
    val fileResource = loadFile(fullPath).map(Parsing.readFile(path, _))
    fileResource.tried.get
  }

  private[this] def getFile(path: PTB3Path): Try[PTB3File] =
    Try(getFileUnsafe(path))

  private[this] def allPTBPaths = Try {
    val wsjPrefix = annotationPath.resolve("WSJ")
    val wsjPaths = for {
      sectionName <- new java.io.File(wsjPrefix.toString).listFiles
        .map(_.getName)
        .iterator
      sectionFolder = new java.io.File(wsjPrefix.resolve(sectionName).toString)
      if sectionFolder.isDirectory
      sectionNumber <- Try(sectionName.toInt).toOption.iterator
      fileName      <- sectionFolder.listFiles.map(_.getName).iterator
      if fileName.endsWith(".MRG")
      // filename format: WSJ_SSNN.MRG
      // where SS is section number and NN is file number
      // so file number is substring at [6, 8)
      fileNumber <- Try(fileName.substring(6, 8).toInt).toOption.iterator
    } yield WSJPath(sectionNumber, fileNumber)

    val brownPrefix = annotationPath.resolve("BROWN")
    val brownPaths = for {
      domain <- new java.io.File(brownPrefix.toString).listFiles
        .map(_.getName)
        .iterator
      domainFolder = new java.io.File(brownPrefix.resolve(domain).toString)
      if domainFolder.isDirectory
      fileName <- domainFolder.listFiles.map(_.getName).iterator
      if fileName.endsWith(".MRG")
      // filename format: DDNN.MRG
      // where DD is domain and NN is file number
      // so file number is substring at [2, 4)
      fileNumber <- Try(fileName.substring(2, 4).toInt).toOption.iterator
    } yield BrownPath(domain, fileNumber)

    (wsjPaths ++ brownPaths).toList
  }
}

class PTB3FileSystemService(
  location: Path
) extends InterpretedPTB3Service[Try](
      new PTB3FileSystemInterpreter(location)
    )
