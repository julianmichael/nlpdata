import mill._, mill.scalalib._, mill.scalalib.publish._, mill.scalajslib._
import mill.scalalib.scalafmt._
import mill.util.Ctx
import coursier.maven.MavenRepository
import ammonite.ops._

val thisPublishVersion = "0.2.0"

val scalaVersions = List("2.11.12", "2.12.6")
val thisScalaJSVersion = "0.6.23"

val kindProjectorVersion = "0.9.4"
val macroParadiseVersion = "2.1.0"

val catsVersion = "1.1.0"
val upickleVersion = "0.4.4"
val fastparseVersion = "0.4.4"
val simulacrumVersion = "0.10.0"
val macmemoVersion = "0.4"
val argonautVersion = "6.2"
val scalaArmVersion = "2.0"
val corenlpVersion = "3.6.0"
val trove4jVersion = "3.0.1"

trait CommonModule extends ScalaModule with ScalafmtModule {

  def platformSegment: String

  def sources = T.sources(
    millSourcePath / "src",
    millSourcePath / s"src-$platformSegment"
  )

  def scalacOptions = Seq(
    "-Ypartial-unification",
    "-language:higherKinds",
    "-unchecked",
    "-deprecation",
    "-feature"
  )

  def ivyDeps = Agg(
    ivy"org.typelevel::cats-core::$catsVersion",
    ivy"org.typelevel::cats-free::$catsVersion",
    ivy"com.lihaoyi::fastparse::$fastparseVersion",
    ivy"com.github.mpilquist::simulacrum::$simulacrumVersion"
  )

  def scalacPluginIvyDeps = super.scalacPluginIvyDeps() ++ Agg(
    ivy"org.spire-math::kind-projector:$kindProjectorVersion",
    ivy"org.scalamacros:::paradise:$macroParadiseVersion"
  )
}

trait NlpdataModule extends CommonModule with PublishModule with CrossScalaModule {

  def millSourcePath = build.millSourcePath / "nlpdata"

  def artifactName = "nlpdata"
  def publishVersion = thisPublishVersion
  def pomSettings = PomSettings(
    description = artifactName(),
    organization = "org.julianmichael",
    url = "https://github.com/julianmichael/nlpdata",
    licenses = Seq(License.MIT),
    versionControl = VersionControl.github("julianmichael", "nlpdata"),
    developers = Seq(
      Developer("julianmichael", "Julian Michael","https://github.com/julianmichael")
    )
  )

  trait NlpdataTestModule extends Tests with CommonModule {
    def platformSegment = NlpdataModule.this.platformSegment
    def ivyDeps = Agg(
      ivy"org.scalatest::scalatest:3.0.1",
      ivy"org.scalacheck::scalacheck:1.13.4",
      ivy"org.typelevel::discipline:0.7.3"
    )
    def testFrameworks = Seq("org.scalatest.tools.Framework")
  }
}

object nlpdata extends Module {
  object jvm extends Cross[NlpdataJvmModule](scalaVersions: _*)
  class NlpdataJvmModule(val crossScalaVersion: String) extends NlpdataModule {
    def platformSegment = "jvm"

    def ivyDeps = super.ivyDeps() ++ Agg(
      ivy"com.softwaremill.macmemo::macros::$macmemoVersion",
      ivy"io.argonaut::argonaut:$argonautVersion",
      ivy"com.jsuereth::scala-arm:$scalaArmVersion",
      ivy"edu.stanford.nlp:stanford-corenlp:$corenlpVersion",
      ivy"com.lihaoyi::upickle:$upickleVersion",
      ivy"net.sf.trove4j:trove4j:$trove4jVersion"
    )

    object test extends NlpdataTestModule
  }
  object js extends Cross[NlpdataJsModule](scalaVersions: _*)
  class NlpdataJsModule(val crossScalaVersion: String) extends NlpdataModule with ScalaJSModule {
    def platformSegment = "js"
    def scalaJSVersion = thisScalaJSVersion

    object test extends NlpdataTestModule
  }
}

