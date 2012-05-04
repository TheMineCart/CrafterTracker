package tmc.CrafterTracker.services

import tmc.CrafterTracker.domain.Session
import com.google.gson.GsonBuilder
import tmc.CrafterTracker.adapters.DateTimeAdapter
import org.joda.time.DateTime
import com.mongodb.util.JSON
import com.mongodb.{BasicDBObject, DBCollection, DBObject}

// Created by cyrus on 5/2/12 at 1:36 PM

class SessionRepository(c: DBCollection) {
  val collection: DBCollection = c
  val gson = new GsonBuilder().registerTypeAdapter(classOf[DateTime], new DateTimeAdapter()).create

  def save(session: Session) {
   val sessionObject = JSON.parse(gson.toJson(session, classOf[Session])).asInstanceOf[DBObject]
   collection.insert(sessionObject)
  }

  def findByPlayerName(playerName: String): List[Session] = {
    val cursor = collection.find(new BasicDBObject("username", playerName))

    var order: BasicDBObject = new BasicDBObject
    order.put("connectedAt", 1)
    cursor.sort(order)

    var sessionList: List[Session] = List()
    while (cursor.hasNext) {
      val dbObject = cursor.next
      sessionList ::= gson.fromJson(dbObject.toString, classOf[Session])
    }
    sessionList
  }

  def findMostRecentByPlayerName(playerName: String): Session = {
    val sessions: List[Session] = findByPlayerName(playerName)
    if (sessions.size == 0) return null
    sessions.head
  }

  def count: Long = collection.count()
}
