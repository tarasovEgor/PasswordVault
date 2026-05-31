package com.example.passwordvault

import cats.effect.{IO, IOApp, Resource}
import com.comcast.ip4s.*
import com.example.passwordvault.config.AppConfig
import com.example.passwordvault.crypto.Crypto
import com.example.passwordvault.db.Database
import com.example.passwordvault.http.{HealthRoutes, PasswordRoutes}
import com.example.passwordvault.repository.PasswordRepository
import com.example.passwordvault.service.PasswordService
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import com.example.passwordvault.http.SwaggerRoutes

object Main extends IOApp.Simple {

  override def run: IO[Unit] =
    AppConfig.load.flatMap { config =>
        {
          val appResource =
            for {
              crypto <- Resource.eval(Crypto.aesGcm(config.masterKeyBase64))

              transactor <- Database.transactor(config.database)

              repository = PasswordRepository.postgres[IO](transactor)

              passwordService = PasswordService.live(
                repository = repository,
                crypto = crypto
              )

              httpApp = Router(
                "/" -> HealthRoutes.routes,
                "/" -> PasswordRoutes.routes(passwordService),
                "/" -> SwaggerRoutes.routes
              ).orNotFound

              server <- EmberServerBuilder
                .default[IO]
                .withHost(host"0.0.0.0")
                .withPort(port"8080")
                .withHttpApp(httpApp)
                .build
            } yield server

          appResource.useForever
        }
    }
}