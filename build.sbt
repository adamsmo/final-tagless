import scalariform.formatter.preferences._

name := "final-tagless"
version := "1.0"
scalaVersion := "2.12.6"

val akkaVersion = "2.5.14"
val akkaHttpVersion = "10.1.4"
val slickVersion = "3.2.3"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.slick" %% "slick" % slickVersion,
  "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
  "org.typelevel" %% "cats-core" % "1.2.0",
  "org.typelevel" %% "cats-effect" % "1.0.0-RC3",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  //DB for demo purpose
  "com.h2database" % "h2" % "1.4.197",
  //libs for tests
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "org.scalacheck" %% "scalacheck" % "1.14.0" % "test"
)

scalariformPreferences := scalariformPreferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(DoubleIndentConstructorArguments, true)
  .setPreference(DanglingCloseParenthesis, Prevent)
  .setPreference(DoubleIndentMethodDeclaration, true)

addCommandAlias("runShop", "runMain Server")

//for cats library
scalacOptions += "-Ypartial-unification"