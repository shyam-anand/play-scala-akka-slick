name := "GCMPlay"

version := "1.0"

lazy val `gcmplay` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq( cache , ws )

libraryDependencies ++= Seq(
  "mysql" % "mysql-connector-java" % "5.1.18",
  "javax.inject" % "javax.inject" % "1",
  "com.typesafe.play" %% "anorm" % "2.4.0",
  "com.typesafe.play" %% "play-slick" % "1.1.1",
  "com.typesafe.play" %% "play-slick-evolutions" % "1.1.1"
)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )