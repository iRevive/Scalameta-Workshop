import sbt.Keys.resolvers
import sbt.Resolver

lazy val root = (project in file("."))
  .settings(commonSettings: _*)
  .settings(
    name := Settings.name,
    libraryDependencies ++= Dependencies.root
  )
  .aggregate(macros)
  .dependsOn(macros)

lazy val macros = (project in file("macros"))
  .settings(commonSettings: _*)
  .settings(
    name := s"${Settings.name}-macros",
    libraryDependencies ++= Dependencies.macros
  )

lazy val commonSettings = Seq(
  organization := Settings.organization,
  scalaVersion := Versions.scala,
  scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-feature",
    "-encoding", "UTF-8",
    "-Xplugin-require:macroparadise",
    "-Ypartial-unification"
  ),
  resolvers ++= List(Resolvers.scalaMeta, Resolver.mavenLocal),
  addCompilerPlugin(Library.scalaMetaParadise)
)
