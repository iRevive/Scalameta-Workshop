import sbt._

object Settings {
  val organization          = "io.github.irevive"
  val name                  = "Scalameta-Workshop"
}

object Versions {
  val scala                 = "2.12.3"

  val scalaMeta             = "1.8.0"
  val scalaMetaParadise     = "3.0.0-M10"
}

object Resolvers {
  val scalaMeta = Resolver.url("scalameta", url("http://dl.bintray.com/scalameta/maven"))(Resolver.ivyStylePatterns)
}

object Library {
  val scalaReflect          = "org.scala-lang" %    "scala-reflect"   % Versions.scala
  val scalaMeta             = "org.scalameta"  %%   "scalameta"       % Versions.scalaMeta
  val scalaMetaParadise     = "org.scalameta"  %    "paradise"        % Versions.scalaMetaParadise cross CrossVersion.full
}

object Dependencies {
  import Library._

  val macros = List(
    scalaReflect,
    scalaMeta
  )

  val root = List(

  )

}