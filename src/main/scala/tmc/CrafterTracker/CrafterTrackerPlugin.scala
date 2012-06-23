package tmc.CrafterTracker

import domain._
import executors._
import listener.{PlayerInteractionListener, PlayerConnectionListener}
import org.bukkit.plugin.java.JavaPlugin
import org.joda.time.Minutes
import services.{PlayerWarningService, PlayerRepository, SessionRepository}

// Created by cyrus on 5/1/12 at 10:18 AM

class CrafterTrackerPlugin extends JavaPlugin {

  override def onEnable() {
    CtPlugin.initialize(this)

    CtPlugin.logger.info("Configuring...")
    Configuration.initialize

    CtPlugin.logger.info("Initializing database...")
    Database.initialize

    if(CtPlugin.plugin.isEnabled) {
      CtPlugin.logger.info("Registering command executors...")
      registerCommandExecutors()

      CtPlugin.logger.info("Registering event listeners...")
      registerEventListeners()

      CtPlugin.logger.info("Setting up new sessions for connected players...")
      setUpSessions()

      CtPlugin.logger.info("Starting player warning service...")
      PlayerWarningService.active = true
      PlayerWarningService.start()

      CtPlugin.logger.info("Initialization complete!")
    }
  }

  override def onDisable() {
    CtPlugin.logger.info("Disabling PlayerWarningService...")
    PlayerWarningService.active = false

    CtPlugin.logger.info("Saving sessions for connected players...")
    tearDownSessions()

    CtPlugin.logger.info("Disabling...")
  }

  def setUpSessions() {
    CtPlugin.server.getOnlinePlayers.foreach(player => {
      SessionMap.put(player.getName, new Session(player.getName, player.getAddress.getAddress.toString))
      persistIfNewPlayer(player.getName)
    })
  }

  private def persistIfNewPlayer(playerName: String) {
    if (!PlayerRepository.exists(playerName)) {
      PlayerRepository.save(new Player(playerName))
    }
  }

  def tearDownSessions() {
    CtPlugin.server.getOnlinePlayers.foreach(
      player => {
        SessionMap.applyToSessionFor(
          player.getName, (s: Session) => {
            s.disconnected
            updatePlayerStatistics(s)
            SessionRepository.save(s)
          }
        )
      }
    )
    SessionMap.clear()
  }

  private def updatePlayerStatistics(s: Session) {
    val persistedPlayer = PlayerRepository.findByPlayerName(s.username)
    persistedPlayer.addBroken(s.blocksBroken)
    persistedPlayer.addPlaced(s.blocksPlaced)
    persistedPlayer.addMinutesPlayed(Minutes.minutesBetween(s.connectedAt, s.disconnectedAt).getMinutes)
    persistedPlayer.calculateScore
    PlayerRepository.save(persistedPlayer)
  }

  def registerCommandExecutors() {
    getCommand("sessioninfo").setExecutor(SessionInformationExecutor)
    getCommand("warn").setExecutor(WarningExecutor)
    getCommand("score").setExecutor(PlayerScoreExecutor)
    getCommand("playerinfo").setExecutor(PlayerInformationExecutor)
    getCommand("warningsfor").setExecutor(WarningInformationExecutor)
    getCommand("acknowledge").setExecutor(AcknowledgementExecutor)
  }

  def registerEventListeners() {
    CtPlugin.server.getPluginManager.registerEvents(PlayerConnectionListener, this)
    CtPlugin.server.getPluginManager.registerEvents(PlayerInteractionListener, this)
  }
}
