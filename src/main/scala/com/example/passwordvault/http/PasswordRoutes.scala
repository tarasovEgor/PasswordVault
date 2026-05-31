package com.example.passwordvault.http

import cats.effect.IO
import com.example.passwordvault.domain.*
import com.example.passwordvault.service.PasswordService
import org.http4s.{HttpRoutes, Response}
import org.http4s.headers.`Content-Type`
import org.http4s.MediaType
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.io.*
import org.http4s.dsl.impl.{OptionalQueryParamDecoderMatcher, QueryParamDecoderMatcher}
import org.http4s.EntityEncoder
import org.typelevel.ci.CIStringSyntax

object SearchQueryMatcher extends QueryParamDecoderMatcher[String]("search")
object ExactMatcher extends OptionalQueryParamDecoderMatcher[Boolean]("exact")

object PasswordRoutes {

  // явный string encoder — не даёт circe перехватить строку
  private given stringEncoder: EntityEncoder[IO, String] =
    EntityEncoder.stringEncoder[IO]

  def routes(service: PasswordService): HttpRoutes[IO] =
    HttpRoutes.of[IO] {

      case request @ POST -> Root / "passwords" =>
        for {
          createRequest <- request.as[CreatePasswordRequest]
          createdEntry  <- service.create(createRequest)
          response      <- Created(createdEntry)
        } yield response

      case GET -> Root / "passwords" / "export" =>
        service.exportCsv.flatMap { csv =>
          Ok(csv)
            .map(
              _.withContentType(`Content-Type`(MediaType.text.csv))
                .putHeaders(
                  org.http4s.headers.`Content-Disposition`(
                    "attachment",
                    Map(ci"filename" -> "passwords.csv")
                  )
                )
            )
        }

      case GET -> Root / "passwords" :? SearchQueryMatcher(search) +& ExactMatcher(exact) =>
        service.search(search, exact.getOrElse(false)).flatMap(Ok(_))

      case GET -> Root / "passwords" =>
        for {
          entries  <- service.list
          response <- Ok(entries)
        } yield response

      case GET -> Root / "passwords" / LongVar(id) =>
        service.get(id).flatMap {
          case Some(entry) => Ok(entry)
          case None        => NotFound(s"Password entry with id=$id was not found")
        }

      case request @ PATCH -> Root / "passwords" / LongVar(id) =>
        for {
          updateRequest <- request.as[UpdatePasswordRequest]
          maybeUpdated  <- service.update(id, updateRequest)
          response <- maybeUpdated match {
            case Some(updated) => Ok(updated)
            case None          => NotFound(s"Password entry with id=$id was not found")
          }
        } yield response

      case DELETE -> Root / "passwords" / LongVar(id) =>
        service.delete(id).flatMap {
          case true  => NoContent()
          case false => NotFound(s"Password entry with id=$id was not found")
        }

      case request @ POST -> Root / "passwords" / "import" =>
        for {
          csvContent <- request.as[String]
          imported   <- service.importCsv(csvContent)
          response   <- Ok(imported)
        } yield response
    }
}