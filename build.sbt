name := "chief-of-state-journal-migration-test"

version := "0.1"

scalaVersion := "2.13.3"


val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.3" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.1.1" % Test

lazy val `notable-service` = (project in file("."))
  .aggregate(
    `notable-api`,
    `notable`,
    `notable-common`
  )

lazy val `notable-common` = (project in file("notable-common"))
  .enablePlugins(AkkaGrpcPlugin)
  .settings(
    name := "notable-common",
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"
    ),
      PB.protoSources in Compile := Seq(file("notable-common/src/main/protobuf")),
      PB.targets in Compile := Seq(scalapb.gen() -> (sourceManaged in Compile).value)
  )

lazy val `notable-api` = (project in file("notable-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )
  .dependsOn(`notable-common`)

lazy val `notable` = (project in file("notable"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings)
  .dependsOn(`notable-api`, `notable-common`)
