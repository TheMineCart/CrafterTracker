package tmc.CrafterTracker.domain

import collection.immutable.HashMap

// Created by cyrus on 5/3/12 at 11:46 AM

object SessionMap extends SessionMap {

}

class SessionMap {
  var sessions: Map[String, Session] = new HashMap[String, Session]

  def get(playerName: String): Session = {
    sessions.get(playerName).get
  }

  def put(playerName: String, session: Session) {
    sessions += playerName -> session
  }

  def remove(playerName: String) {
    sessions -= playerName
  }
}
