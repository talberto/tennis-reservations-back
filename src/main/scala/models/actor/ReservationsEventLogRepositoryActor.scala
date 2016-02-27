package models.actor

import java.util.UUID

import akka.actor._
import akka.pattern.pipe
import models.db.ReservationsEventLogRepository
import play.api.Logger

import scala.concurrent.Future
import scala.language.implicitConversions

object ReservationsEventLogRepositoryActor {
  val props = Props[ReservationsEventLogRepositoryActor]

  case class SaveEvent(event: ReservationAggregateActor.Event)

  case class EventSaved(event: ReservationAggregateActor.Event)

  case class FindEvents(reservationId: UUID)

  case class EventsFound(events: Seq[ReservationAggregateActor.Event])

}

/**
  * Repository for Booking
  */
class ReservationsEventLogRepositoryActor extends Actor {

  import ReservationsEventLogRepositoryActor._
  import context.dispatcher

  val logger: Logger = Logger(this.getClass)

  override def receive: Receive = {
    case SaveEvent(evt) =>
      logger.debug(s"Will add event $evt to event log")
      val eventualResponse = ReservationsEventLogRepository.add(evt).map(_ => EventSaved(evt))
      pipe(eventualResponse) to sender

    case FindEvents(reservationId) =>
      val eventualEvents = ReservationsEventLogRepository.findAllEvents(reservationId)
      val eventualMsg = eventualEvents.map(events => EventsFound(events))
      pipe(eventualMsg) to sender
  }
}
