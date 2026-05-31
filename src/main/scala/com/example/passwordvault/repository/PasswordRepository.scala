package com.example.passwordvault.repository

import cats.effect.kernel.MonadCancelThrow
import cats.syntax.functor.*
import com.example.passwordvault.domain.PasswordRecord
import doobie.*
import doobie.implicits.*

trait PasswordRepository[F[_]] {

  def create(
              name: String,
              encryptedPassword: String,
              comment: Option[String],
              created: Long
            ): F[PasswordRecord]

  def list: F[List[PasswordRecord]]

  def get(id: Long): F[Option[PasswordRecord]]

  def update(
              id: Long,
              name: Option[String],
              encryptedPassword: Option[String],
              comment: Option[Option[String]]
            ): F[Option[PasswordRecord]]

  def delete(id: Long, deleted: Long): F[Boolean]
}

object PasswordRepository {

  def postgres[F[_]: MonadCancelThrow](
                                        xa: Transactor[F]
                                      ): PasswordRepository[F] =
    new PasswordRepository[F] {

      private type Row =
        (Long, String, String, Option[String], Long, Option[Long])

      private def toRecord(row: Row): PasswordRecord = {
        val (id, name, encryptedPassword, comment, created, deleted) = row

        PasswordRecord(
          id = id,
          name = name,
          encryptedPassword = encryptedPassword,
          comment = comment,
          created = created,
          deleted = deleted
        )
      }

      override def create(
                           name: String,
                           encryptedPassword: String,
                           comment: Option[String],
                           created: Long
                         ): F[PasswordRecord] =
        sql"""
          INSERT INTO password_entries (
            name,
            encrypted_password,
            comment,
            created
          )
          VALUES (
            $name,
            $encryptedPassword,
            $comment,
            $created
          )
          RETURNING id, name, encrypted_password, comment, created, deleted
        """
          .query[Row]
          .map(toRecord)
          .unique
          .transact(xa)

      override def list: F[List[PasswordRecord]] =
        sql"""
          SELECT id, name, encrypted_password, comment, created, deleted
          FROM password_entries
          WHERE deleted IS NULL
          ORDER BY id ASC
        """
          .query[Row]
          .map(toRecord)
          .to[List]
          .transact(xa)

      override def get(id: Long): F[Option[PasswordRecord]] =
        sql"""
          SELECT id, name, encrypted_password, comment, created, deleted
          FROM password_entries
          WHERE id = $id
            AND deleted IS NULL
        """
          .query[Row]
          .map(toRecord)
          .option
          .transact(xa)

      override def update(
                           id: Long,
                           name: Option[String],
                           encryptedPassword: Option[String],
                           comment: Option[Option[String]]
                         ): F[Option[PasswordRecord]] = {
        val updateComment = comment.isDefined
        val commentValue = comment.flatten

        sql"""
          UPDATE password_entries
          SET
            name = COALESCE($name, name),
            encrypted_password = COALESCE($encryptedPassword, encrypted_password),
            comment =
              CASE
                WHEN $updateComment THEN $commentValue
                ELSE comment
              END
          WHERE id = $id
            AND deleted IS NULL
          RETURNING id, name, encrypted_password, comment, created, deleted
        """
          .query[Row]
          .map(toRecord)
          .option
          .transact(xa)
      }

      override def delete(id: Long, deleted: Long): F[Boolean] =
        sql"""
          UPDATE password_entries
          SET deleted = $deleted
          WHERE id = $id
            AND deleted IS NULL
        """
          .update
          .run
          .transact(xa)
          .map(_ > 0)
    }
}