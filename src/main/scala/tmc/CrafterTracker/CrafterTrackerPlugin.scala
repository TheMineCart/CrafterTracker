package tmc.CrafterTracker

import domain.{Player, Session, SessionMap}
import executors.{WarningExecutor, SessionInformationExecutor}
import listener.{PlayerInteractionListener, PlayerConnectionListener}
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.Server
import java.util.logging.Logger
import java.rmi.UnknownHostException
import com.mongodb.{DB, Mongo}
import org.joda.time.Minutes
import services.{WarningMessageRepository, PlayerRepository, SessionRepository}

// Created by cyrus on 5/1/12 at 10:18 AM

class CrafterTrackerPlugin extends JavaPlugin {

  var server: Server = null
  var logger: Logger = null
  var sessionRepository: SessionRepository = null
  var playerRepository: PlayerRepository = null
  var warningMessageRepository: WarningMessageRepository = null
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
    setUpSessions()
  }

  override def onDisable() {
    tearDownSessions()
  }

  def setUpSessions() {
    server.getOnlinePlayers.foreach(player => {
      SessionMap.put(player.getName, new Session(player.getName, player.getAddress.toString))
      persistIfNewPlayer(player.getName)
    })
  }

  private def persistIfNewPlayer(playerName: String) {
    if (!playerRepository.exists(playerName)) {
      playerRepository.save(new Player(playerName))
    }
  }

  def tearDownSessions() {
    server.getOnlinePlayers.foreach(player => {
      SessionMap.applyToSessionFor(player.getName,
        (s: Session) => { s.disconnected; sessionRepository.save(s); playerRepository.save(updatePlayerStatistics(s))}
      )
    })
    SessionMap.clear()
  }

  private def updatePlayerStatistics(s: Session): Player = {
    val persistedPlayer = playerRepository.findByPlayerName(s.username)
    persistedPlayer.addBroken(s.blocksBroken)
    persistedPlayer.addPlaced(s.blocksPlaced)
    persistedPlayer.addMinutesPlayed(Minutes.minutesBetween(s.connectedAt, s.disconnectedAt).getMinutes)
    persistedPlayer.calculateScore
    persistedPlayer
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
    warningMessageRepository = new WarningMessageRepository(crafterTrackerDB.getCollection("WarningMessages"))
  }

  def initializeCollectionIndexes() = {}

  def registerCommandExecutors() = {
    getCommand("playerinfo").setExecutor(new SessionInformationExecutor)
    getCommand("warn").setExecutor(new WarningExecutor(server, playerRepository, warningMessageRepository))
  }

  def registerEventListeners() {
    server.getPluginManager().registerEvents(new PlayerConnectionListener(sessionRepository, playerRepository), this)
    server.getPluginManager().registerEvents(new PlayerInteractionListener, this)
  }
}
