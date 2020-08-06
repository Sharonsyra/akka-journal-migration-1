name := "chief-of-state-journal-migration-test"

version  in ThisBuild := "0.1"

scalaVersion in ThisBuild := "2.13.3"


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
    PB.protoSources in Compile :=Seq(file("notable-common/src/main/protobuf")),
    akkaGrpcGeneratedLanguages := Seq(AkkaGrpc.Scala),
    akkaGrpcGeneratedSources := Seq(AkkaGrpc.Server, AkkaGrpc.Client),
    akkaGrpcCodeGeneratorSettings := akkaGrpcCodeGeneratorSettings.value.filterNot(_ == "flat_package"),
    akkaGrpcCodeGeneratorSettings += "server_power_apis"
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
