package com.namely.notable

import akka.actor.ActorSystem
import akka.grpc.GrpcServiceException
import com.namely.protobuf.chief_of_state.{Event, HandleReadSideRequest, HandleReadSideResponse, ReadSideHandlerService}
import io.grpc.Status
import org.slf4j.LoggerFactory

import scala.concurrent.Future

class ReadSideHandlerServiceImpl extends ReadSideHandlerService {

  // Boot akka
  implicit val sys = ActorSystem("NotableClient")
  implicit val ec = sys.dispatcher

  val log = LoggerFactory.getLogger(classOf[ReadSideHandlerServiceImpl])

  /**
   * Helps build a read model from persisted events and snpahots
   */
  override def handleReadSide(in: HandleReadSideRequest): Future[HandleReadSideResponse] = {

    val incomingEvent = in.event match {
      case Some(value) => Event.parseFrom(value.toByteArray)
      case None =>
        throw new GrpcServiceException(Status.UNKNOWN.withDescription("Invalid event sent to ReadSide"))
    }



    Future(
      HandleReadSideResponse(
        successful = true
      )
    )
  }
}
