package tmc.CrafterTracker.services

import com.google.gson.GsonBuilder
import org.joda.time.DateTime
import com.mongodb.util.JSON
import com.mongodb.{BasicDBObject, DBObject, DBCollection}
import tmc.CrafterTracker.domain.{Infraction, WarningMessage}
import tmc.CrafterTracker.adapters.{InfractionAdapter, DateTimeAdapter}

// Created by cyrus on 5/8/12 at 1:52 PM

class WarningMessageRepository(c: DBCollection) {

  val collection = c
  val gson = new GsonBuilder()
    .registerTypeAdapter(classOf[DateTime], new DateTimeAdapter)
    .registerTypeAdapter(classOf[Infraction], new InfractionAdapter)
    .create

  def save(message: WarningMessage) {
   val warningObject = JSON.parse(gson.toJson(message, classOf[WarningMessage])).asInstanceOf[DBObject]
   collection.insert(warningObject)
  }

  def findByPlayerName(playerName: String): List[WarningMessage] = {
    buildList(new BasicDBObject("recipient", playerName))
  }

  def findUnacknowledgedByPlayerName(playerName: String) : List[WarningMessage] = {
    var query = new BasicDBObject
    query.put("recipient", playerName)
    query.put("acknowledged", false)
    buildList(query)
  }

  private def buildList(query: BasicDBObject): List[WarningMessage] = {
    var warningList : List[WarningMessage] = List()
    val cursor = collection.find(query)
    cursor.sort(new BasicDBObject("issuedAt", -1))

    while(cursor.hasNext) {
      val dbObject = cursor.next
      warningList ::= gson.fromJson(dbObject.toString, classOf[WarningMessage])
    }

    warningList
  }

  def count: Long = collection.count()
}
