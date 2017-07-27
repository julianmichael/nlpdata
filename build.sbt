lazy val root = project.in(file("."))
  .aggregate(nlpdataJVM, nlpdataJS)
  .settings(
  publish := {},
  publishLocal := {})

lazy val nlpdata = crossProject.settings(
  name := "nlpdata",
  version := "0.1-SNAPSHOT",
  publishSettings,
  scalaVersion in ThisBuild := "2.11.8", // TODO cross-build
  scalacOptions ++= Seq("-language:higherKinds", "-deprecation", "-feature", "-unchecked"),
  resolvers += Resolver.sonatypeRepo("snapshots"),
  libraryDependencies += "org.typelevel" %%% "cats" % "0.9.0",
  libraryDependencies += "com.softwaremill.macmemo" %% "macros" % "0.4-SNAPSHOT",
  libraryDependencies += "com.lihaoyi" %%% "fastparse" % "0.3.7",
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.13.4" % "test",
  libraryDependencies += "org.typelevel" %% "discipline" % "0.7.3" % "test",
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.4")
).jvmSettings(
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
  libraryDependencies += "io.argonaut" %% "argonaut" % "6.2-SNAPSHOT" changing(),
  libraryDependencies += "com.jsuereth" % "scala-arm_2.11" % "2.0-RC1",
  libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % "3.6.0",
  libraryDependencies += "com.lihaoyi" %%% "upickle" % "0.4.1",
  // java
  libraryDependencies += "net.sf.trove4j" % "trove4j" % "3.0.1"
)

lazy val nlpdataJS = nlpdata.js
lazy val nlpdataJVM = nlpdata.jvm

// TODO organizationHomepage, organizationName, start year?, licenses, other information...
// TODO make nicely publishable either on jitpack or maven central.
// probably maven central once I figure out the right groupId, etc.
lazy val publishSettings = Seq(
  organization := "com.github.julianmichael",
  homepage := Some(url("https://github.com/julianmichael/nlpdata")),
  publishMavenStyle := true,
  scmInfo := Some(
    ScmInfo(url("https://github.com/julianmichael/nlpdata"),
            "scm:git:https://github.com:julianmichael/nlpdata.git")),
  pomExtra := (
    <developers>
      <developer>
        <id>julianmichael</id>
        <name>Julian Michael</name>
      </developer>
    </developers>
  )
)
