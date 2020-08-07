package com.namely.notable

import akka.actor.ActorSystem
import com.google.protobuf.any.Any
import com.namely.protobuf.chief_of_state.HandleCommandResponse.ResponseType

import scala.concurrent.Future
import com.namely.protobuf.chief_of_state._
import com.namely.protobuf.notable._

class WriteSideHandler  extends WriteSideHandlerService {

  // Boot akka
  implicit val sys = ActorSystem("NotableClient")
  implicit val ec = sys.dispatcher

  /**
   * Processes every command sent by ChiefOfState and return either a response
   * containing an event to persist or a simple reply.
   */
  override def handleCommand(in: HandleCommandRequest): Future[HandleCommandResponse] = {
    sys.log.info("Handling received command")
    val state = in.currentState.map(_.unpack[Note]).getOrElse(Note.defaultInstance)
    val meta = in.meta.getOrElse(MetaData.defaultInstance)

    in.command.getOrElse(None) match {
      case createCommand: CreateNoteRequest =>
        handleCreateNote(createCommand, state, meta)
      case getCommand: GetNoteRequest =>
        handleGetNote(getCommand, state, meta)
      case changeCommand: ChangeNoteRequest =>
        handleChangeNote(changeCommand, state, meta)
      case deleteCommand: DeleteNoteRequest =>
        handleDeleteNote(deleteCommand, state, meta)
    }
  }

  /**
   * Processes every event sent by ChiefOfState by applying the event to the
   * current state to return a new state.
   */
  override def handleEvent(in: HandleEventRequest): Future[HandleEventResponse] = {
    sys.log.info("Handling received event")

    val currentState = in.currentState.map(_.unpack[Note]).getOrElse(Note.defaultInstance)
    val meta = in.meta.getOrElse(MetaData.defaultInstance)

    in.event.getOrElse(Event) match {
      case createEvent: NoteCreated =>
        handleNoteCreated(createEvent, currentState, meta)
      case changeEvent: NoteChanged =>
        handleNoteChanged(changeEvent, currentState, meta)
      case deleteEvent: NoteDeleted =>
        handleNoteDeleted(deleteEvent, currentState, meta)
    }
  }

  private def handleCreateNote(
                                command: CreateNoteRequest,
                                state: Note,
                                metaData: MetaData
                              ): Future[HandleCommandResponse] = {
    require(command.noteTitle.nonEmpty, "Note title is required!")

    val noteCreatedEvent: NoteCreated =
      NoteCreated()
        .withNoteId(command.noteId)
        .withNoteTitle(command.noteTitle)
        .withNoteContent(command.noteContent)

    val any: Any = Any.pack(noteCreatedEvent)

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

  private def handleNoteCreated(event: NoteCreated, state: Note, metaData: MetaData): Future[HandleEventResponse] = {
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

  private def handleNoteChanged(event: NoteChanged, state: Note, metaData: MetaData): Future[HandleEventResponse] = {
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

