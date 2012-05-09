package tmc.CrafterTracker

import domain.{Session, SessionMap}
import executors.SessionInformationExecutor
import listener.{PlayerInteractionListener, PlayerConnectionListener}
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.Server
import java.util.logging.Logger
import java.rmi.UnknownHostException
import com.mongodb.{DB, Mongo}
import services.{PlayerRepository, SessionRepository}

// Created by cyrus on 5/1/12 at 10:18 AM

class CrafterTrackerPlugin extends JavaPlugin {

  var server: Server = null
  var logger: Logger = null
  var sessionRepository: SessionRepository = null
  var playerRepository: PlayerRepository = null
  var mongoConnection: Mongo = null
  var crafterTrackerDB: DB = null

  override def onEnable() {
    server = getServer
    logger = getLogger()
    initializeMongoDB()
    initializeDatabase()
    initializeRepositories()
    initializeCollectionIndexes()
    registerCommandExecutors()
    registerEventListeners()
    setupSessionMap()
  }

  override def onDisable() {
    tearDownSessionMap()
  }

  def setupSessionMap() {
    server.getOnlinePlayers.foreach(player => {
      SessionMap.put(player.getName, new Session(player.getName, player.getAddress.toString))
    })
  }

  def tearDownSessionMap(){
    server.getOnlinePlayers.foreach(player => {
      SessionMap.applyToSessionFor(player.getName,
        (s:Session) => { s.disconnected; sessionRepository.save(s) })
    })
    SessionMap.clear()
  }

  def initializeMongoDB() {
    try
      mongoConnection = new Mongo("127.0.0.1")
    catch {
      case u: UnknownHostException => logger.warning("Something went wrong!")
    }
  }

  def initializeDatabase() =
    crafterTrackerDB = mongoConnection.getDB("CrafterTracker")

  def initializeRepositories() {
    sessionRepository = new SessionRepository(crafterTrackerDB.getCollection("Sessions"))
    playerRepository = new PlayerRepository(crafterTrackerDB.getCollection("Players"))
  }

  def initializeCollectionIndexes() = {}

  def registerCommandExecutors() =
    getCommand("playerinfo").setExecutor(new SessionInformationExecutor)

  def registerEventListeners() {
    server.getPluginManager().registerEvents(new PlayerConnectionListener(sessionRepository, playerRepository), this)
    server.getPluginManager().registerEvents(new PlayerInteractionListener, this)
  }

}
