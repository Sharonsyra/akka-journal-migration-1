package com.namely.notable

import slick.jdbc.PostgresProfile.api._
import akka.actor.ActorSystem
import akka.grpc.GrpcServiceException
import com.namely.protobuf.chief_of_state.{Event, HandleReadSideRequest, HandleReadSideResponse, ReadSideHandlerService}
import io.grpc.Status
import org.slf4j.LoggerFactory
import slick.lifted.TableQuery

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class ReadSideHandlerServiceImpl extends ReadSideHandlerService {

  // Boot akka
  implicit val sys = ActorSystem("NotableClient")
  implicit val ec = sys.dispatcher

  val log = LoggerFactory.getLogger(classOf[ReadSideHandlerServiceImpl])

  val journalTable: TableQuery[JournalTable] = TableQuery[JournalTable]

  /**
   * Helps build a read model from persisted events and snpahots
   */
  override def handleReadSide(in: HandleReadSideRequest): Future[HandleReadSideResponse] = {

    log.info(s"Handling Received event on ReadSide ${in.event.get}")

    val incomingEvent = in.event match {
      case Some(value) => Event.parseFrom(value.toByteArray)
      case None =>
        throw new GrpcServiceException(Status.UNKNOWN.withDescription("Invalid event sent to ReadSide"))
    }

    Try(
      journalTable
        .map(
          t => (t.ordering, t.persistenceId, t.sequenceNumber, t.deleted, t.tags, t.message)
        ) += (Some(1), "", 1, false, Some(""), incomingEvent.toByteArray)
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
}
