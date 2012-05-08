package tmc.CrafterTracker.domain

import collection.mutable.HashMap

// Created by cyrus on 5/3/12 at 11:46 AM

object SessionMap extends SessionMap {

}

class SessionMap {
  var sessions: HashMap[String, Session] = new HashMap[String, Session]

  def get(playerName: String): Option[Session] = {
    sessions.get(playerName)
  }

  def put(playerName: String, session: Session) {
    sessions += playerName -> session
  }

  def remove(playerName: String) {
    sessions -= playerName
  }

  def clear() {
    sessions = sessions.empty
  }
}
