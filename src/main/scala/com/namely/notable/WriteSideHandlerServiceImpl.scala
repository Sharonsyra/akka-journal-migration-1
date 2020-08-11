package com.namely.notable

import akka.actor.ActorSystem
import akka.grpc.GrpcServiceException
import com.google.protobuf.any.Any
import com.namely.protobuf.chief_of_state.HandleCommandResponse.ResponseType

import scala.concurrent.Future
import com.namely.protobuf.chief_of_state._
import com.namely.protobuf.notable._
import io.grpc.Status
import org.slf4j.LoggerFactory

class WriteSideHandlerServiceImpl  extends WriteSideHandlerService {

  // Boot akka
  implicit val sys = ActorSystem("NotableClient")
  implicit val ec = sys.dispatcher

  val log = LoggerFactory.getLogger(classOf[WriteSideHandlerServiceImpl])

  /**
   * Processes every command sent by ChiefOfState and return either a response
   * containing an event to persist or a simple reply.
   */
  override def handleCommand(in: HandleCommandRequest): Future[HandleCommandResponse] = {

    log.info(s"Handling received command ${in.command.get}")
    val priorState: Note = in.currentState match {
      case Some(value) =>
        log.info(s"State value is: ${Note.parseFrom(value.toByteArray)}")
        Note.parseFrom(value.toByteArray)
      case None => Note.defaultInstance
    }
//      in.currentState.getOrElse(Note.defaultInstance)
    log.info(s"Handle Command state value is: ${priorState}")
//    val priorState: Note = Note.parseFrom(state.toByteArray)
//    log.info(s"Handle Command prior state value is: ${priorState}")
    //    val state: Note = in.currentState.map(_.unpack[Note]).getOrElse(Note.defaultInstance)
    val priorMetaData = in.meta match {
      case Some(value) => value
      case None => MetaData.defaultInstance
      }
//    val priorMeta: MetaData = in.meta.getOrElse(MetaData.defaultInstance)
    log.info(s"Handle Command Metadata value is: ${priorMetaData}")

    in.command match {
      case Some(value) => log.info(s" Handle command Some command => $value")
        value match {
        case createNoteRequest: Any if value.is[CreateNoteRequest] =>
          handleCreateNote(createNoteRequest.unpack[CreateNoteRequest], priorState, priorMetaData)
        case getNoteRequest: Any if value.is[GetNoteRequest] =>
          handleGetNote(getNoteRequest.unpack[GetNoteRequest], priorState, priorMetaData)
        case changeNoteRequest: Any if value.is[ChangeNoteRequest] =>
          handleChangeNote(changeNoteRequest.unpack[ChangeNoteRequest], priorState, priorMetaData)
        case deleteNoteRequest: Any if value.is[DeleteNoteRequest] =>
          handleDeleteNote(deleteNoteRequest.unpack[DeleteNoteRequest], priorState, priorMetaData)
        case _ => throw new GrpcServiceException(Status.INTERNAL.withDescription("Unknown command"))
      }
      case None => throw new GrpcServiceException(Status.INTERNAL.withDescription("No command sent"))
    }
  }

  /**
   * Processes every event sent by ChiefOfState by applying the event to the
   * current state to return a new state.
   */
  override def handleEvent(in: HandleEventRequest): Future[HandleEventResponse] = {
    log.info("Handling received event")

//    val priotState = in.currentState.map(_.unpack[Note]).getOrElse(Note.defaultInstance)
//    val priorMeta = in.meta.getOrElse(MetaData.defaultInstance)

    val priorState: Note = in.currentState match {
      case Some(value) =>
        log.info(s"State value is: ${Note.parseFrom(value.toByteArray)}")
        Note.parseFrom(value.toByteArray)
      case None => Note.defaultInstance
    }

    val priorMetaData = in.meta match {
      case Some(value) => value
      case None => MetaData.defaultInstance
    }

    in.event match {
      case Some(value) => log.info(s"Handle event Some event => $value")
        value match {
          case noteCreated: Any if value.is[NoteCreated] =>
            handleNoteCreated(noteCreated.unpack[NoteCreated], priorState, priorMetaData)
          case noteChanged: Any if value.is[NoteChanged] =>
            handleNoteChanged(noteChanged.unpack[NoteChanged], priorState, priorMetaData)
          case noteDeleted: Any if value.is[NoteDeleted] =>
            handleNoteDeleted(noteDeleted.unpack[NoteDeleted], priorState, priorMetaData)
          case _ => throw new GrpcServiceException(Status.INTERNAL.withDescription("Unknown event"))
        }
      case None => throw new GrpcServiceException(Status.INTERNAL.withDescription("No event sent"))
    }

  }

  private def handleCreateNote(
    command: CreateNoteRequest,
    state: Note,
    metaData: MetaData
  ): Future[HandleCommandResponse] = {

    log.info("In command handler")

    require(command.noteTitle.nonEmpty, "Note title is required!")

    val noteCreatedEvent: NoteCreated =
      NoteCreated()
        .withNoteId(command.noteId)
        .withNoteTitle(command.noteTitle)
        .withNoteContent(command.noteContent)

    log.info(s"Packed event in handleCreateNote ${Any.pack(noteCreatedEvent)}")

    val any: Any = Any.pack(noteCreatedEvent)

    log.info("Sending Handle Command Response ........ ")

    Future(
      HandleCommandResponse()
        .withResponseType(
          ResponseType
            .PersistAndReply(
              PersistAndReply()
                .withEvent(any)
            )
        )
    )
  }

  private def handleGetNote(
    command: GetNoteRequest,
    state: Note,
    metaData: MetaData
  ): Future[HandleCommandResponse] = {

    require(command.noteId.nonEmpty, "Note ID is required!")
    require(command.noteId.equals(state.noteId), "Wrong Note Id sent!")

    log.info("Persisting Handle Command response...... ")

    Future(
      HandleCommandResponse()
        .withResponseType(
          ResponseType
            .Reply(
              Reply()
            )
        )
    )
  }

  private def handleChangeNote(
    command: ChangeNoteRequest,
    state: Note,
    metaData: MetaData
  ): Future[HandleCommandResponse] = {
    require(command.noteId.nonEmpty, "Note ID is required!")
    require(command.noteId.equals(state.noteId), "Wrong Note Id sent!")

    val noteChangedEvent =
      NoteChanged()
        .withNoteId(command.noteId)
        .withNoteTitle(command.noteTitle)
        .withNoteContent(command.noteContent)

    val any: Any = Any.pack(noteChangedEvent)

    Future(
      HandleCommandResponse()
        .withResponseType(
          ResponseType
            .PersistAndReply(
              PersistAndReply()
                .withEvent(any)
            )
        )
    )
  }

  private def handleDeleteNote(
    command: DeleteNoteRequest,
    state: Note,
    data: MetaData
  ): Future[HandleCommandResponse] = {
    require(command.noteId.nonEmpty, "Note ID is required!")
    require(command.noteId.equals(state.noteId), "Wrong Note Id sent!")

    val noteDeletedEvent =
      NoteDeleted()
        .withNoteId(command.noteId)

    val any: Any = Any.pack(noteDeletedEvent)

    Future(
      HandleCommandResponse()
        .withResponseType(
          ResponseType
            .PersistAndReply(
              PersistAndReply()
                .withEvent(any)
            )
        )
    )
  }

  private def handleNoteCreated(
    event: NoteCreated,
    state: Note,
    metaData: MetaData
  ): Future[HandleEventResponse] = {
    val updatedStated =
      state.update(
        _.noteId := event.noteId,
        _.noteTitle := event.noteTitle,
        _.noteContent := event.noteContent
      )

    val any = Any.pack(updatedStated)

    Future(
      HandleEventResponse()
        .withResultingState(any)
    )
  }

  private def handleNoteChanged(
    event: NoteChanged,
    state: Note,
    metaData: MetaData
  ): Future[HandleEventResponse] = {
    val updatedStated =
      state.update(
        _.noteId := event.noteId,
        _.noteTitle := event.noteTitle,
        _.noteContent := event.noteContent
      )

    val any = Any.pack(updatedStated)

    Future(
      HandleEventResponse()
        .withResultingState(any)
    )
  }

  private def handleNoteDeleted(event: NoteDeleted, state: Note, metaData: MetaData): Future[HandleEventResponse] = {
    val updatedStated =
      state.update(
        _.noteId := event.noteId,
        _.isDeleted := true
      )

    val any = Any.pack(updatedStated)

    Future(
      HandleEventResponse()
        .withResultingState(any)
    )
  }
}

