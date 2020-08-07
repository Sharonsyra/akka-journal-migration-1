package com.namely.notable

import java.util.UUID

import com.namely.protobuf.chief_of_state._
import com.namely.protobuf.notable._
import com.namely.protobuf.notable.grpc._
import akka.actor.ActorSystem
import akka.grpc.GrpcClientSettings
import com.google.protobuf.any.Any

import scala.concurrent.Future

class NotableServiceImpl extends NotableService {

  // Boot akka
  implicit val sys = ActorSystem("NotableClient")
  implicit val ec = sys.dispatcher

  // Configure the client
  val clientSettings = GrpcClientSettings.fromConfig("namely.chief_of_state.ChiefOfStateService")

  // Create a client-side stub for the service
  val client = ChiefOfStateServiceClient(clientSettings)

  override def createNote(in: CreateNoteRequest): Future[Note] = {

    sys.log.info(s"Creating Note ${in.noteTitle}")

    val entityUuid = UUID.randomUUID().toString

    val cmd =
      CreateNoteRequest()
        .withNoteId(entityUuid)
        .withNoteTitle(in.noteTitle)
        .withNoteContent(in.noteContent)

    val processCmd =
      ProcessCommandRequest()
        .withEntityId(entityUuid)
        .withCommand(Any.pack(cmd))

    sys.log.info("Sending command to chief of state")

    val reply: Future[ProcessCommandResponse] = client.processCommand(processCmd)

    reply.map { response =>
      response.state match {
        case Some(value) => value.unpack[Note]
        case None => Note.defaultInstance
      }}

  }

  override def getNote(in: GetNoteRequest): Future[Note] = {
    sys.log.info(s"Retrieving Note ${in.noteId}")

    val cmd =
      GetNoteRequest()
        .withNoteId(in.noteId)

    val processCmd =
      ProcessCommandRequest()
        .withEntityId(in.noteId)
        .withCommand(Any.pack(cmd))

    sys.log.info("Sending command to chief of state")

    val reply: Future[ProcessCommandResponse] = client.processCommand(processCmd)

    reply.map { response =>
      response.state match {
        case Some(value) => value.unpack[Note]
        case None => Note.defaultInstance
      }}

  }

  override def changeNote(in: ChangeNoteRequest): Future[Note] = {
    sys.log.info(s"Updating Note ${in.noteId}")

    val cmd =
      ChangeNoteRequest()
        .withNoteId(in.noteId)
        .withNoteTitle(in.noteTitle)
        .withNoteContent(in.noteContent)

    val processCmd =
      ProcessCommandRequest()
        .withEntityId(in.noteId)
        .withCommand(Any.pack(cmd))

    sys.log.info("Sending command to chief of state")

    val reply: Future[ProcessCommandResponse] = client.processCommand(processCmd)

    reply.map { response =>
      response.state match {
        case Some(value) => value.unpack[Note]
        case None => Note.defaultInstance
      }}
  }

  override def deleteNote(in: DeleteNoteRequest): Future[Note] = {
    sys.log.info(s"Deleting Note ${in.noteId}")

    val cmd =
      DeleteNoteRequest()
        .withNoteId(in.noteId)

    val processCmd =
      ProcessCommandRequest()
        .withEntityId(in.noteId)
        .withCommand(Any.pack(cmd))

    sys.log.info("Sending command to chief of state")

    val reply: Future[ProcessCommandResponse] = client.processCommand(processCmd)

    reply.map { response =>
      response.state match {
        case Some(value) => value.unpack[Note]
        case None => Note.defaultInstance
      }}
  }
}
