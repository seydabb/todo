import play.sbt.PlayImport._
import sbt.{ModuleID, Test}
import sbt._

object Dependencies {

  val commonDependencies: Seq[ModuleID] = Seq(
    jdbc,
    evolutions,
    ehcache,
    ws,
    guice,
    "org.postgresql" % "postgresql" % "42.2.2",
    "com.typesafe.play" %% "anorm" % "2.5.3"
  )

  val testDependencies: Seq[ModuleID] = Seq(
    "org.scalatest" %% "scalatest" % "3.0.1",
    "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2",
    "org.mockito" % "mockito-core" % "2.7.22"
  ).map(_.%(Test))

  val appDependencies: Seq[ModuleID] = commonDependencies ++ testDependencies
}
