name := "journal-migration"

version := "0.1"

scalaVersion := "2.13.3"

libraryDependencies ++= Seq(
  "org.postgresql" % "postgresql" % "9.3-1102-jdbc41",
  "io.superflat" %% "lagompb-core" % "0.4.0",
  "io.superflat" %% "lagompb-core" % "0.4.0" % "protobuf"
//  "com.typesafe.slick" %% "slick" % "3.2.0",
//  "org.slf4j" % "slf4j-nop" % "1.6.4",
//  "com.typesafe.slick" %% "slick-hikaricp" % "3.2.0"
)
