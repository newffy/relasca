import sbt._
import Keys._

object BuildSettings {
  val paradiseVersion = "2.0.1"
  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := "ru.simplesys",
    version := "1.0.0",
    scalacOptions ++= Seq("-deprecation", "-explaintypes", "-feature", "-language:postfixOps", "-language:implicitConversions", "-language:higherKinds"),
    scalaVersion := "2.11.2",
    addCompilerPlugin("org.scalamacros" % "paradise" % paradiseVersion cross CrossVersion.full)
  )
}

object ThisBuild extends Build {
  import BuildSettings._

  lazy val root: Project = Project(
    "relasca",
    file("."),
    settings = buildSettings ++ Seq(
    libraryDependencies ++= {
    Seq(
//            "com.chuusai" % "shapeless" % "2.0.0-SNAPSHOT" cross CrossVersion.full changing(),
              "org.scalaz" %% "scalaz-core" % "7.0.6",
//
//        "com.simplesys" %% "common" % "1.0.0-SNAPSHOT" % "test",
        "joda-time" % "joda-time" % "2.4",
        "org.joda" % "joda-convert" % "1.7",
        "org.scalatest" %% "scalatest" % "2.2.1" % "test"
    )
}

    )
  ) dependsOn(macros)

  lazy val macros: Project = Project(
    "macros",
    file("macros"),
    settings = buildSettings ++ Seq(
      libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-reflect" % _),
      libraryDependencies += "com.chuusai" %% "shapeless" % "2.0.0" changing()// cross CrossVersion.full changing()
    )
  )
}
