package com.example.passwordvault.config

import cats.effect.IO

final case class DatabaseConfig(
                                 url: String,
                                 user: String,
                                 password: String
                               )

final case class AppConfig(
                            database: DatabaseConfig,
                            masterKeyBase64: String
                          )

object AppConfig {

  def load: IO[AppConfig] =
    for {
      dbUrl <- requiredEnv("DB_URL")
      dbUser <- requiredEnv("DB_USER")
      dbPassword <- requiredEnv("DB_PASSWORD")
      masterKeyBase64 <- requiredEnv("MASTER_KEY_BASE64")
    } yield AppConfig(
      database = DatabaseConfig(
        url = dbUrl,
        user = dbUser,
        password = dbPassword
      ),
      masterKeyBase64 = masterKeyBase64
    )

  private def requiredEnv(name: String): IO[String] =
    IO.fromOption(sys.env.get(name))(
      new IllegalArgumentException(s"Missing required environment variable: $name")
    )
}