package tmc.CrafterTracker.services

import com.google.gson.GsonBuilder
import org.joda.time.DateTime
import com.mongodb.util.JSON
import tmc.CrafterTracker.domain.{Infraction, WarningMessage}
import tmc.CrafterTracker.adapters.{InfractionAdapter, DateTimeAdapter}
import java.util.regex.Pattern
import com.mongodb.{DBCollection, BasicDBObject, DBObject}

// Created by cyrus on 5/8/12 at 1:52 PM

object WarningMessageRepository {

  var collection: DBCollection = null
  val gson = new GsonBuilder()
    .registerTypeAdapter(classOf[DateTime], DateTimeAdapter)
    .registerTypeAdapter(classOf[Infraction], InfractionAdapter)
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

  def acknowledgeWarningFor(playerName: String, timestamp: DateTime): Boolean = {
    val timeQuery = Pattern.compile(timestamp.toString("yyyy-MM-dd") + "T" + timestamp.toString("HH:mm"))

    var query = new BasicDBObject
    query.put("recipient", playerName)
    query.put("acknowledged", false)
    query.put("issuedAt", timeQuery)

    buildList(query).foreach(warning => {
      warning.acknowledge
      val warningObject = JSON.parse(gson.toJson(warning, classOf[WarningMessage])).asInstanceOf[DBObject]
      updateWhere(warning.recipient, timeQuery, warningObject )
      return true
    })
    false
  }

  private def updateWhere(recipient: String, timestampPattern: Pattern, warningObject: DBObject) {
    val updateWhere = new BasicDBObject()
    updateWhere.put("recipient", recipient)
    updateWhere.put("issuedAt", timestampPattern)
    collection.update(updateWhere, warningObject, true, false)
  }

  private def buildList(query: BasicDBObject): List[WarningMessage] = {
    var warningList : List[WarningMessage] = List()
    val cursor = collection.find(query)
    cursor.sort(new BasicDBObject("issuedAt", 1))

    while(cursor.hasNext) {
      val dbObject = cursor.next
      warningList ::= gson.fromJson(dbObject.toString, classOf[WarningMessage])
    }

    warningList
  }

  def count: Long = collection.count()
}
