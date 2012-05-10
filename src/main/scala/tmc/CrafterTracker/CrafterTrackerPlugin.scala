package tmc.CrafterTracker

import domain.{Player, Session, SessionMap}
import executors.{WarningExecutor, SessionInformationExecutor}
import listener.{PlayerInteractionListener, PlayerConnectionListener}
import org.bukkit.plugin.java.JavaPlugin
import org.joda.time.Minutes
import services.{PlayerRepository, SessionRepository}

// Created by cyrus on 5/1/12 at 10:18 AM

class CrafterTrackerPlugin extends JavaPlugin {

  override def onEnable() {
    CtPlugin.server = getServer
    CtPlugin.logger = getLogger
    registerCommandExecutors()
    registerEventListeners()
    setUpSessions()
  }

  override def onDisable() {
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
    getCommand("sessioninfo").setExecutor(new SessionInformationExecutor())
    getCommand("warn").setExecutor(new WarningExecutor)
  }

  def registerEventListeners() {
    CtPlugin.server.getPluginManager.registerEvents(new PlayerConnectionListener, this)
    CtPlugin.server.getPluginManager.registerEvents(new PlayerInteractionListener, this)
  }
}
