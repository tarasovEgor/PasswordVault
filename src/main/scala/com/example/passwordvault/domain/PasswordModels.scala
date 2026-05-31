package com.example.passwordvault.domain

import io.circe.{Decoder, Encoder, HCursor}
import io.circe.generic.semiauto.*

final case class PasswordEntry(
                                id: Long,
                                name: String,
                                password: String,
                                comment: Option[String],
                                created: Long,
                                deleted: Option[Long]
                              )

object PasswordEntry {
  given Encoder[PasswordEntry] = deriveEncoder[PasswordEntry]
}

final case class CreatePasswordRequest(
                                        name: String,
                                        password: String,
                                        comment: Option[String]
                                      )

object CreatePasswordRequest {
  given Decoder[CreatePasswordRequest] = deriveDecoder[CreatePasswordRequest]
}

final case class UpdatePasswordRequest(
                                        name: Option[String],
                                        password: Option[String],
                                        comment: Option[Option[String]]
                                      )

object UpdatePasswordRequest {

  given Decoder[UpdatePasswordRequest] = Decoder.instance { cursor =>
    val commentCursor = cursor.downField("comment")

    val decodedComment: Decoder.Result[Option[Option[String]]] =
      if (!commentCursor.succeeded) {
        Right(None)
      } else {
        commentCursor.focus match {
          case Some(json) if json.isNull =>
            Right(Some(None))

          case _ =>
            commentCursor.as[String].map(value => Some(Some(value)))
        }
      }

    for {
      name <- cursor.get[Option[String]]("name")
      password <- cursor.get[Option[String]]("password")
      comment <- decodedComment
    } yield UpdatePasswordRequest(
      name = name,
      password = password,
      comment = comment
    )
  }
}

final case class PasswordRecord(
                                 id: Long,
                                 name: String,
                                 encryptedPassword: String,
                                 comment: Option[String],
                                 created: Long,
                                 deleted: Option[Long]
                               )