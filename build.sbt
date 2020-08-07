name := "chief-of-state-journal-migration-test"

version  in ThisBuild := "0.1"

scalaVersion in ThisBuild := "2.13.3"

mainClass in (Compile, run) := Some("com.namely.notable.Notable")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % "2.6.8",
  "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"
)

// Akka dependencies used by Lagom
dependencyOverrides ++= Seq(
  "com.typesafe.akka" %% "akka-discovery" % "2.6.8",
  "com.typesafe.akka" %% "akka-stream" % "2.6.8",
  "com.typesafe.akka" %% "akka-protobuf-v3" % "2.6.8",

)

PB.protoSources in Compile := Seq(
  file("chief-of-state-protos/chief_of_state"),
  file("src/main/protobuf")
)

enablePlugins(AkkaGrpcPlugin)
