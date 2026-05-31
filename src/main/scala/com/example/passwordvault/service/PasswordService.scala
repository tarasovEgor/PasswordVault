package com.example.passwordvault.service

import cats.effect.{Clock, IO, Ref}
import com.example.passwordvault.domain.*

trait PasswordService {
  def create(request: CreatePasswordRequest): IO[PasswordEntry]

  def list: IO[List[PasswordEntry]]

  def get(id: Long): IO[Option[PasswordEntry]]

  def update(id: Long, request: UpdatePasswordRequest): IO[Option[PasswordEntry]]

  def delete(id: Long): IO[Boolean]
}

object PasswordService {

  private final case class State(
                                  nextId: Long,
                                  items: Map[Long, PasswordEntry]
                                )

  def inMemory: IO[PasswordService] =
    Ref.of[IO, State](State(nextId = 1L, items = Map.empty)).map { ref =>
      new PasswordService {

        override def create(request: CreatePasswordRequest): IO[PasswordEntry] =
          currentTimestamp.flatMap { now =>
            ref.modify { state =>
              val entry = PasswordEntry(
                id = state.nextId,
                name = request.name,
                password = request.password,
                comment = request.comment,
                created = now,
                deleted = None
              )

              val updatedState = state.copy(
                nextId = state.nextId + 1,
                items = state.items + (entry.id -> entry)
              )

              updatedState -> entry
            }
          }

        override def list: IO[List[PasswordEntry]] =
          ref.get.map { state =>
            state.items.values
              .filter(_.deleted.isEmpty)
              .toList
              .sortBy(_.id)
          }

        override def get(id: Long): IO[Option[PasswordEntry]] =
          ref.get.map { state =>
            state.items.get(id).filter(_.deleted.isEmpty)
          }

        override def update(id: Long, request: UpdatePasswordRequest): IO[Option[PasswordEntry]] =
          ref.modify { state =>
            state.items.get(id).filter(_.deleted.isEmpty) match {
              case Some(current) =>
                val updated = current.copy(
                  name = request.name.getOrElse(current.name),
                  password = request.password.getOrElse(current.password),
                  comment = request.comment match {
                    case Some(newComment) => newComment
                    case None             => current.comment
                  }
                )

                val updatedState = state.copy(
                  items = state.items.updated(id, updated)
                )

                updatedState -> Some(updated)

              case None =>
                state -> None
            }
          }

        override def delete(id: Long): IO[Boolean] =
          currentTimestamp.flatMap { now =>
            ref.modify { state =>
              state.items.get(id).filter(_.deleted.isEmpty) match {
                case Some(current) =>
                  val deleted = current.copy(deleted = Some(now))

                  val updatedState = state.copy(
                    items = state.items.updated(id, deleted)
                  )

                  updatedState -> true

                case None =>
                  state -> false
              }
            }
          }

        private def currentTimestamp: IO[Long] =
          Clock[IO].realTime.map(_.toSeconds)
      }
    }
}