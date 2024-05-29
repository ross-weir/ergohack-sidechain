name := "ergohack-sidechain"
version := "0.1"
scalaVersion := "2.12.18"

resolvers ++= Seq(
  "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "SonaType" at "https://oss.sonatype.org/content/groups/public",
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  "New Sonatype Releases" at "https://s01.oss.sonatype.org/content/repositories/releases/",
  "Bintray" at "https://jcenter.bintray.com/"
)

libraryDependencies ++= Seq(
  "org.scorexfoundation" %% "sigma-state" % "5.0.14-60-a90e7bea-20240527-1732-SNAPSHOT",
  "org.ergoplatform" %% "ergo-core" % "5.0.21-0-1a417f44-20240530-0003-SNAPSHOT",
  "org.scalatest" %% "scalatest" % "3.2.18" % Test
)
