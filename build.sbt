name := "ergohack-sidechain"
version := "0.1"
scalaVersion := "2.12.17"

resolvers ++= Seq(
  "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "SonaType" at "https://oss.sonatype.org/content/groups/public",
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  "New Sonatype Releases" at "https://s01.oss.sonatype.org/content/repositories/releases/",
  "Bintray" at "https://jcenter.bintray.com/"
)

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-parser-combinators" % "2.2.0",
  "org.scalactic" %% "scalactic" % "3.2.15",
  "org.scalatest" %% "scalatest" % "3.2.15" % "test",
  "org.scorexfoundation" %% "sigma-state" % "5.0.14-60-a90e7bea-20240527-1732-SNAPSHOT"
)
