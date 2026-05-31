package com.example.passwordvault.domain

import io.circe.{Decoder, Encoder}
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
  given Decoder[UpdatePasswordRequest] = deriveDecoder[UpdatePasswordRequest]
}

final case class PasswordRecord(
                                 id: Long,
                                 name: String,
                                 encryptedPassword: String,
                                 comment: Option[String],
                                 created: Long,
                                 deleted: Option[Long]
                               )