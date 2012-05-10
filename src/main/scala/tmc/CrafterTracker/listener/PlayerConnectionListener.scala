package tmc.CrafterTracker.listener

import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.event.player.{PlayerLoginEvent, PlayerQuitEvent}
import tmc.CrafterTracker.services.{PlayerRepository, SessionRepository}
import tmc.CrafterTracker.domain.{Player, SessionMap, Session}
import org.joda.time.Minutes

// Created by cyrus on 5/1/12 at 2:05 PM

object PlayerConnectionListener extends Listener {

  @EventHandler
  def onPlayerConnect(event: PlayerLoginEvent) {
    val playerName = event.getPlayer.getName
    val ipAddress = event.getAddress.toString
    SessionMap.put(playerName, new Session(playerName, ipAddress))

    if (!PlayerRepository.exists(playerName))
      PlayerRepository.save(new Player(playerName))
  }

  @EventHandler
  def onPlayerDisconnect(event: PlayerQuitEvent) {
    SessionMap.applyToSessionFor(event.getPlayer.getName,
      (s:Session) => { s.disconnected; SessionRepository.save(s); updatePlayerStats(s) })
    SessionMap.remove(event.getPlayer.getName)
  }

  private def updatePlayerStats(session: Session) {
    if (!PlayerRepository.exists(session.username)) return

    var player = PlayerRepository.findByPlayerName(session.username)
    val duration = Minutes.minutesBetween(session.connectedAt, session.disconnectedAt).getMinutes
    player.addMinutesPlayed(duration)
    player.addBroken(session.blocksBroken)
    player.addPlaced(session.blocksPlaced)
    player.calculateScore
    PlayerRepository.save(player)
  }
}
