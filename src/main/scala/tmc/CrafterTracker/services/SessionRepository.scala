package tmc.CrafterTracker.services

import tmc.CrafterTracker.domain.Session
import com.mongodb.DBCollection

// Created by cyrus on 5/2/12 at 1:36 PM

class SessionRepository(c: DBCollection) {
  var sessions: List[Session] = List()
  val collection: DBCollection = c

  def save(session: Session) {
    sessions ::= session
  }

  def findByPlayerName(playerName: String): List[Session] = {
     sessions.filter(s => (true))
  }

}
