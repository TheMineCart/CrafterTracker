package tmc.CrafterTracker

import java.rmi.UnknownHostException
import com.mongodb.{DB, Mongo}
import services.{SessionRepository, WarningMessageRepository, PlayerRepository}

// Created by cyrus on 5/10/12 at 10:34 AM

object Database {

  var mongoConnection: Mongo = null
  var db: DB = null

  def initialize() {
    try {
      mongoConnection = new Mongo(Configuration.dbAddress)
      CtPlugin.logger.info("Found MongoDB instance at " + Configuration.dbAddress)
      initializeDatabase()
      initializeRepositories()
    } catch {
      case u: UnknownHostException => CtPlugin.logger.warning("Something went wrong when trying to initialize the database!" + u.toString)
    }
  }

  private def initializeDatabase() {
    CtPlugin.logger.info("Connecting to database " + Configuration.dbName + ".")
    db = mongoConnection.getDB(Configuration.dbName)

    if(Configuration.dbUser != null) {
      CtPlugin.logger.info("Attempting authentication to database " + Configuration.dbName + " with user " + Configuration.dbUser + ".")
      val success = db.authenticate(Configuration.dbUser, Configuration.dbPassword.toCharArray)
      if (success) {
        CtPlugin.logger.info("Connection to database " + Configuration.dbName + " with authentication was successful!")
      } else {
        CtPlugin.logger.warning("Incorrect Mongo Database Authentication Info: " +
                                "please double check the settings in your config.yml file.")
      }
    } else {
      CtPlugin.logger.info("Connection to database " + Configuration.dbName + " without authentication was successful!")
    }
  }

  private def initializeRepositories() {
    CtPlugin.logger.info("Initializing repositories...")
    PlayerRepository.collection = Database.db.getCollection("Players")
    WarningMessageRepository.collection = Database.db.getCollection("WarningMessages")
    SessionRepository.collection = Database.db.getCollection("Sessions")
  }
}
