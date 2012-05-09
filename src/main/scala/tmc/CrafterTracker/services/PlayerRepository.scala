package tmc.CrafterTracker.services

import com.google.gson.GsonBuilder
import tmc.CrafterTracker.adapters.DateTimeAdapter
import org.joda.time.DateTime
import com.mongodb.util.JSON
import tmc.CrafterTracker.domain.Player
import com.mongodb.{BasicDBObject, DBObject, DBCollection}

// Created by cyrus on 5/9/12 at 9:44 AM

class PlayerRepository(c: DBCollection) {
  val collection: DBCollection = c
  val gson = new GsonBuilder().registerTypeAdapter(classOf[DateTime], new DateTimeAdapter).create

  def save(player: Player) {
    val playerObject = JSON.parse(gson.toJson(player, classOf[Player])).asInstanceOf[DBObject]
    val updateWhere = new BasicDBObject("username", player.username)
    collection.update(updateWhere, playerObject, true, false)
  }

  def findByPlayerName(playerName: String): Player = {
    val playerObject: DBObject = collection.findOne(new BasicDBObject("username", playerName))
    if (playerObject == null) return null
    gson.fromJson(playerObject.toString, classOf[Player])
  }

  def exists(playerName: String): Boolean = {
    if (findByPlayerName(playerName) == null) return false
    true
  }

  def count(): Long = {
    collection.count()
  }
}
