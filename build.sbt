name := "todoApp"
 
version := "1.0" 
      
lazy val `todoapp` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
      
scalaVersion := "2.12.6"

libraryDependencies ++= Seq(
  jdbc,
  ehcache,
  ws,
  guice,
  evolutions,
  "org.postgresql" % "postgresql" % "42.2.2",
  "com.typesafe.play" %% "anorm" % "2.5.3",
  "org.scalatest" %% "scalatest" % "3.0.5"
)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

