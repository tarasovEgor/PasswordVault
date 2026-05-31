package com.example.passwordvault

import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.all.*
import com.comcast.ip4s.*
import com.example.passwordvault.http.{HealthRoutes, PasswordRoutes}
import com.example.passwordvault.service.PasswordService
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits.*

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    PasswordService.inMemory.flatMap { passwordService =>
      val httpApp =
        (
          HealthRoutes.routes <+>
            PasswordRoutes.routes(passwordService)
          ).orNotFound

      EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(httpApp)
        .build
        .use { _ =>
          IO.println("Password Vault service started on http://localhost:8080") >>
            IO.never
        }
        .as(ExitCode.Success)
    }
}