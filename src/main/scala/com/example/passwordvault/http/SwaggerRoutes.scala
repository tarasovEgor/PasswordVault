package com.example.passwordvault.http

import cats.effect.IO
import org.http4s.HttpRoutes
import sttp.tapir.*
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.server.http4s.Http4sServerInterpreter

object SwaggerRoutes {

  // эндпоинты для доки
  private val endpoints = List(
    endpoint.get
      .in("health")
      .out(stringBody)
      .description("Health check"),

    endpoint.get
      .in("passwords")
      .out(stringBody)
      .description("List all password entries"),

    endpoint.post
      .in("passwords")
      .in(stringBody)
      .out(stringBody)
      .description("Create a new password entry"),

    endpoint.get
      .in("passwords" / path[Long]("id"))
      .out(stringBody)
      .description("Get password entry by ID"),

    endpoint.patch
      .in("passwords" / path[Long]("id"))
      .in(stringBody)
      .out(stringBody)
      .description("Update password entry"),

    endpoint.delete
      .in("passwords" / path[Long]("id"))
      .out(stringBody)
      .description("Delete password entry"),

    endpoint.get
      .in("passwords")
      .in(query[String]("search"))
      .in(query[Boolean]("exact"))
      .out(stringBody)
      .description("Search passwords by name"),

    endpoint.get
      .in("passwords" / "export")
      .out(stringBody)
      .description("Export all passwords as CSV"),

    endpoint.post
      .in("passwords" / "import")
      .in(stringBody)
      .out(stringBody)
      .description("Import passwords from CSV"),
  )

  val routes: HttpRoutes[IO] =
    Http4sServerInterpreter[IO]().toRoutes(
      SwaggerInterpreter().fromEndpoints[IO](endpoints, "Password Vault API", "1.0.0")
    )
}