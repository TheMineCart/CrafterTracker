package tmc.CrafterTracker.domain

import collection.mutable.HashMap

// Created by cyrus on 5/3/12 at 11:46 AM

object SessionMap {
  val sessions: HashMap[String, Session] = new HashMap[String, Session]

  def get(playerName: String): Option[Session] =
    sessions.get(playerName)

  def put(playerName: String, session: Session) =
    sessions += playerName -> session

  def remove(playerName: String) =
    sessions -= playerName

  def clear() =
    sessions.clear()

  def applyToSessionFor(playerName: String, function:(Session) => Unit) =
    sessions.get(playerName).map(function)

  def size(): Int = {
    sessions.size
  }
}
