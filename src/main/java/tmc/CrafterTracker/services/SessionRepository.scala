package tmc.CrafterTracker.services

import tmc.CrafterTracker.domain.Session
import scala.collection.mutable
import collection.mutable.Set

// Created by cyrus on 5/2/12 at 1:36 PM

class SessionRepository {
  var sessions: List[Session] = List()

  def save(session: Session) {
    sessions ::= session
  }

  def findByPlayerName(playerName: String): List[Session] = {
     sessions.filter(s => (true))
  }

}
