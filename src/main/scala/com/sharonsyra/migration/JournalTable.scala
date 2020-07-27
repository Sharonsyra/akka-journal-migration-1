//package com.sharonsyra.migration
//
//import com.typesafe.config.{Config, ConfigFactory}
//
//import slick.jdbc.PostgresProfile.api._
//
//object Set extends App {
//
////  val config: Config = ConfigFactory.parseResources("db.conf").resolve()
////  val db = Database.forConfig("db", config)
//
//  private val dbname = "postgres"
//  private val driver = "org.postgresql.Driver"
//  val con_st = "jdbc:postgresql://localhost:5432/postgres?user=postgres&password=changeme"
//
//  private val postgres = Database.forURL(
//    s"jdbc:postgresql:${dbname}?user=postgres&password=changeme",
//    driver = driver,
//    executor = AsyncExecutor("%s".format(dbname), numThreads=10, queueSize=1000)
//  )
//
//
////  sql"select persistence_id, deleted, tags from journal".as[(String, Boolean, String)]
////  postgres.run(action).list
//
//}
//
//class JournalTable extends Table[(BigInt, String, BigInt, Boolean, String, Array[Byte])] {
//  def * =
//    (ordering, deleted, persistenceId, sequenceNumber, message, tags)
//  val ordering: Rep[Long] = column[Long](journalTableCfg.columnNames.ordering, O.AutoInc)
//  val deleted: Rep[Boolean] = column[Boolean](journalTableCfg.columnNames.deleted, O.Default(false))
//  val persistenceId: Rep[String] =
//    column[String](journalTableCfg.columnNames.persistenceId, O.Length(255, varying = true))
//  val sequenceNumber: Rep[Long] = column[Long](journalTableCfg.columnNames.sequenceNumber)
//  val message: Rep[Array[Byte]] = column[Array[Byte]](journalTableCfg.columnNames.message)
//  val tags: Rep[Option[String]] =
//    column[Option[String]](journalTableCfg.columnNames.tags, O.Length(255, varying = true))
//}
//
