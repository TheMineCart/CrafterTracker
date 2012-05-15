package tmc.CrafterTracker

import domain.{Player, Session, SessionMap}
import executors._
import listener.{PlayerInteractionListener, PlayerConnectionListener}
import org.bukkit.plugin.java.JavaPlugin
import org.joda.time.Minutes
import services.{PlayerWarningService, PlayerRepository, SessionRepository}

// Created by cyrus on 5/1/12 at 10:18 AM

class CrafterTrackerPlugin extends JavaPlugin {

  override def onEnable() {
    CtPlugin.plugin = this
    CtPlugin.server = getServer
    CtPlugin.logger = getLogger
    registerCommandExecutors()
    registerEventListeners()
    setUpSessions()
    PlayerWarningService.active = true
    PlayerWarningService.start()
  }

  override def onDisable() {
    PlayerWarningService.active = false
    tearDownSessions()
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
    CtPlugin.server.getOnlinePlayers.foreach(player => {
      SessionMap.applyToSessionFor(player.getName,
        (s: Session) => { s.disconnected; SessionRepository.save(s); PlayerRepository.save(updatePlayerStatistics(s))}
      )
    })
    SessionMap.clear()
  }

  private def updatePlayerStatistics(s: Session): Player = {
    val persistedPlayer = PlayerRepository.findByPlayerName(s.username)
    persistedPlayer.addBroken(s.blocksBroken)
    persistedPlayer.addPlaced(s.blocksPlaced)
    persistedPlayer.addMinutesPlayed(Minutes.minutesBetween(s.connectedAt, s.disconnectedAt).getMinutes)
    persistedPlayer.calculateScore
    persistedPlayer
  }

  def registerCommandExecutors() = {
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
