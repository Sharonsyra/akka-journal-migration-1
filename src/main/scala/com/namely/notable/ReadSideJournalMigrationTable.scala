package com.namely.notable

import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

final case class JournalEntity (
  ordering: Option[Long],
  persistenceId: String,
  sequenceNumber: Long,
  deleted: Boolean,
  tags: Option[String] = None,
  message: Array[Byte]
)

class JournalTable(tag: Tag) extends Table[JournalEntity](_tableTag = tag, None, _tableName = "new_journal") {
  override def * : ProvenShape[JournalEntity] =
    (
      ordering,
      persistenceId,
      sequenceNumber,
      deleted,
      tags,
      message
    ) <> (JournalEntity.tupled, JournalEntity.unapply)

  def ordering: Rep[Option[Long]] = column[Option[Long]]("ordering", O.AutoInc)
  def persistenceId: Rep[String] =
    column[String]("persistence_id", O.Length(255, varying = true))
  def sequenceNumber: Rep[Long] = column[Long]("sequence_number")
  def deleted: Rep[Boolean] = column[Boolean]("deleted", O.Default(false))
  def tags: Rep[Option[String]] =
    column[Option[String]]("tags", O.Length(255, varying = true))
  def message: Rep[Array[Byte]] = column[Array[Byte]]("message")
  def pk = primaryKey(s"${tableName}_pk", (persistenceId, sequenceNumber))
  def orderingIdx = index(s"${tableName}_ordering_idx", ordering, unique = true)
}
