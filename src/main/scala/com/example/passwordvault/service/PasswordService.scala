package com.example.passwordvault.service

import cats.effect.IO
import cats.syntax.traverse.*
import com.example.passwordvault.crypto.Crypto
import com.example.passwordvault.domain.*
import com.example.passwordvault.repository.PasswordRepository

trait PasswordService {

  def create(request: CreatePasswordRequest): IO[PasswordEntry]

  def list: IO[List[PasswordEntry]]

  def get(id: Long): IO[Option[PasswordEntry]]

  def update(id: Long, request: UpdatePasswordRequest): IO[Option[PasswordEntry]]

  def delete(id: Long): IO[Boolean]
}

object PasswordService {

  def live(
            repository: PasswordRepository[IO],
            crypto: Crypto
          ): PasswordService =
    new PasswordService {

      override def create(request: CreatePasswordRequest): IO[PasswordEntry] =
        for {
          now <- nowSeconds
          encryptedPassword <- crypto.encrypt(request.password)

          record <- repository.create(
            name = request.name,
            encryptedPassword = encryptedPassword,
            comment = request.comment,
            created = now
          )

          entry <- toEntry(record)
        } yield entry

      override def list: IO[List[PasswordEntry]] =
        repository.list.flatMap(_.traverse(toEntry))

      override def get(id: Long): IO[Option[PasswordEntry]] =
        repository.get(id).flatMap {
          case Some(record) =>
            toEntry(record).map(Some(_))

          case None =>
            IO.pure(None)
        }

      override def update(
                           id: Long,
                           request: UpdatePasswordRequest
                         ): IO[Option[PasswordEntry]] =
        for {
          encryptedPassword <- request.password.traverse(crypto.encrypt)

          updatedRecord <- repository.update(
            id = id,
            name = request.name,
            encryptedPassword = encryptedPassword,
            comment = request.comment
          )

          updatedEntry <- updatedRecord.traverse(toEntry)
        } yield updatedEntry

      override def delete(id: Long): IO[Boolean] =
        for {
          now <- nowSeconds
          deleted <- repository.delete(id = id, deleted = now)
        } yield deleted

      private def toEntry(record: PasswordRecord): IO[PasswordEntry] =
        crypto.decrypt(record.encryptedPassword).map { password =>
          PasswordEntry(
            id = record.id,
            name = record.name,
            password = password,
            comment = record.comment,
            created = record.created,
            deleted = record.deleted
          )
        }

      private def nowSeconds: IO[Long] =
        IO.realTime.map(_.toSeconds)
    }
}