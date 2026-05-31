ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.7"
ThisBuild / organization := "com.example"

lazy val catsEffectVersion = "3.7.0"
lazy val http4sVersion = "0.23.34"
lazy val circeVersion = "0.14.15"
lazy val doobieVersion = "1.0.0-RC12"

lazy val root = (project in file("."))
  .settings(
    name := "password-vault-scala",

    libraryDependencies ++= Seq(
      // Effect runtime
      "org.typelevel" %% "cats-effect" % catsEffectVersion,

      // HTTP server
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.http4s" %% "http4s-dsl"          % http4sVersion,
      "org.http4s" %% "http4s-circe"        % http4sVersion,

      // JSON
      "io.circe" %% "circe-core"    % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser"  % circeVersion,

      // PostgreSQL access
      "org.tpolecat" %% "doobie-core"     % doobieVersion,
      "org.tpolecat" %% "doobie-postgres" % doobieVersion,
      "org.tpolecat" %% "doobie-hikari"   % doobieVersion,

      // Logging
      "ch.qos.logback" % "logback-classic" % "1.5.33",

      // Swagger-UI
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server"     % "1.11.7",
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle"  % "1.11.7",
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe"         % "1.11.7",
    ),

    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-unchecked"
    )
  )

Compile / run / fork := true

Compile / run / javaOptions ++= Seq(
  "-Djava.net.useSystemProxies=false",
  "-Dhttp.proxyHost=",
  "-Dhttps.proxyHost=",
  "-DsocksProxyHost="
)