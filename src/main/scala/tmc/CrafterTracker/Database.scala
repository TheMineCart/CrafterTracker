package tmc.CrafterTracker

import java.rmi.UnknownHostException
import com.mongodb.{DB, Mongo}

// Created by cyrus on 5/10/12 at 10:34 AM

object Database {

  var mongoConnection: Mongo = null

  try
    mongoConnection = new Mongo("127.0.0.1")
  catch {
    case u: UnknownHostException => CtPlugin.logger.warning("Something went wrong!")
  }

  val db: DB = mongoConnection.getDB("CrafterTracker")
}
