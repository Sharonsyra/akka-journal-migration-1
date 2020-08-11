package com.namely.notable

import slick.jdbc.PostgresProfile.api._
import akka.actor.ActorSystem
import akka.grpc.GrpcServiceException
import akka.grpc.scaladsl.Metadata
import com.namely.protobuf.chief_of_state.{Event, HandleReadSideRequest, HandleReadSideResponse, MetaData, ReadSideHandlerService, ReadSideHandlerServicePowerApi}
import io.grpc.Status
import org.slf4j.LoggerFactory
import slick.lifted.TableQuery

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class ReadSideHandlerServiceImpl extends ReadSideHandlerServicePowerApi {

  // Boot akka
  implicit val sys = ActorSystem("NotableClient")
  implicit val ec = sys.dispatcher

  val log = LoggerFactory.getLogger(classOf[ReadSideHandlerServiceImpl])

  log.info("To the readside pronto")

  val journalTable: TableQuery[JournalTable] = TableQuery[JournalTable]

  override def handleReadSide(in: HandleReadSideRequest, metadata: Metadata): Future[HandleReadSideResponse] = {

    log.info(s"Handling Received event on ReadSide ${in.event.get}")

    HandleReadSideRequest()
      .withEvent(in.getEvent)
      .withState(in.getState)
      .withMeta(
        MetaData()
          .withEntityId(in.getMeta.entityId)
          .withData(in.getMeta.data)
          .withRevisionDate(in.getMeta.getRevisionDate)
          .withRevisionNumber(in.getMeta.revisionNumber)
      )

    Try(
      journalTable.insertOrUpdate(
        JournalEntity(
          ordering = None,
          persistenceId = persistenceId("Note", metadata.getText("x-cos-entity-id").getOrElse("")),
          sequenceNumber = in.getMeta.revisionNumber,
          deleted = false,
          tags = Some(metadata.getText("x-cos-event-tag").getOrElse("")),
          message = in.getEvent.toByteArray
        )
      )
    ) match {
      case Success(_) =>
        Future(
          HandleReadSideResponse(
            successful = true
          )
        )
      case Failure(exception) =>
        throw new GrpcServiceException(Status.ABORTED.withDescription(exception.getMessage))
    }

  }

  private def persistenceId(entityName: String, entityId: String): String =
    s"$entityName|$entityId"

}
