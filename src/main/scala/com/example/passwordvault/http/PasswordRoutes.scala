package com.example.passwordvault.http

import cats.effect.IO
import com.example.passwordvault.domain.*
import com.example.passwordvault.service.PasswordService
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.io.*

object PasswordRoutes {

  def routes(service: PasswordService): HttpRoutes[IO] =
    HttpRoutes.of[IO] {

      case request @ POST -> Root / "passwords" =>
        for {
          createRequest <- request.as[CreatePasswordRequest]
          createdEntry  <- service.create(createRequest)
          response      <- Created(createdEntry)
        } yield response

      case GET -> Root / "passwords" =>
        for {
          entries  <- service.list
          response <- Ok(entries)
        } yield response

      case GET -> Root / "passwords" / LongVar(id) =>
        service.get(id).flatMap {
          case Some(entry) =>
            Ok(entry)

          case None =>
            NotFound(s"Password entry with id=$id was not found")
        }

      case request @ PATCH -> Root / "passwords" / LongVar(id) =>
        for {
          updateRequest <- request.as[UpdatePasswordRequest]
          maybeUpdated  <- service.update(id, updateRequest)
          response <- maybeUpdated match {
            case Some(updated) =>
              Ok(updated)

            case None =>
              NotFound(s"Password entry with id=$id was not found")
          }
        } yield response

      case DELETE -> Root / "passwords" / LongVar(id) =>
        service.delete(id).flatMap {
          case true =>
            NoContent()

          case false =>
            NotFound(s"Password entry with id=$id was not found")
        }
    }
}