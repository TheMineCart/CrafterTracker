// Copyright (C) 2012 Cyrus Innovation
package tmc.CrafterTracker

import services.{SessionRepository, WarningMessageRepository, PlayerRepository}
import com.mongodb.{DB, Mongo}

// Created by cyrus on 5/10/12 at 10:34 AM

object Database {

  var mongoConnection: Mongo = null
  var db: DB = null

  def initialize() {
    try {
      mongoConnection = new Mongo(Configuration.dbAddress)
      CtPlugin.logger.info("Found MongoDB instance at " + Configuration.dbAddress + ".")
      initializeDatabase()
      initializeRepositories()
    } catch {
      case e: Exception => {
        CtPlugin.logger.warning("Could not find MongoDB instance at " + Configuration.dbAddress + ".")
        CtPlugin.logger.info("Is the dbAddress configured properly in this plugin's config.yml file?")
        CtPlugin.logger.info("Is there an instance of MongoDB installed and running?")
        CtPlugin.logger.info("Disabling due to critical error!!!")
        CtPlugin.shutdown()
      }
    }
  }

  private def initializeDatabase() {
    CtPlugin.logger.info("Connecting to database " + Configuration.dbName + "...")
    db = mongoConnection.getDB(Configuration.dbName)

    if(Configuration.dbUser != null) {
      CtPlugin.logger.info("Attempting authentication to database " + Configuration.dbName + " with user " + Configuration.dbUser + ".")
      val success = db.authenticate(Configuration.dbUser, Configuration.dbPassword.toCharArray)
      if (success) {
        CtPlugin.logger.info("Connection to database " + Configuration.dbName + " with authentication was successful!")
      } else {
        CtPlugin.logger.warning("Incorrect MongoDB authentication info.")
        CtPlugin.logger.info("Please double check the settings this plugin's config.yml file.")
        CtPlugin.logger.info("Disabling due to critical error!!!")
        CtPlugin.shutdown()
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
