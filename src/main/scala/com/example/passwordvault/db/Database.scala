package com.example.passwordvault.db

import cats.effect.{IO, Resource}
import com.example.passwordvault.config.DatabaseConfig
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts

object Database {

  def transactor(config: DatabaseConfig): Resource[IO, HikariTransactor[IO]] =
    for {
      connectEc <- ExecutionContexts.fixedThreadPool[IO](32)

      xa <- HikariTransactor.newHikariTransactor[IO](
        driverClassName = "org.postgresql.Driver",
        url = config.url,
        user = config.user,
        pass = config.password,
        connectEC = connectEc
      )
    } yield xa
}