lazy val root = project.in(file("."))
  .aggregate(nlpdataJVM, nlpdataJS)
  .settings(
  publish := {},
  publishLocal := {})

lazy val nlpdata = crossProject.settings(
  name := "nlpdata",
  organization := "org.me", // TODO com.github.uwnlp?
  version := "0.1-SNAPSHOT",
  scalaVersion in ThisBuild := "2.11.8", // TODO cross-build
  scalacOptions ++= Seq("-language:higherKinds", "-deprecation", "-feature", "-unchecked"),
  resolvers += Resolver.sonatypeRepo("snapshots"),
  libraryDependencies += "org.typelevel" %%% "cats" % "0.9.0",
  libraryDependencies += "com.softwaremill.macmemo" %% "macros" % "0.4-SNAPSHOT",
  libraryDependencies += "com.lihaoyi" %%% "fastparse" % "0.3.7"
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
