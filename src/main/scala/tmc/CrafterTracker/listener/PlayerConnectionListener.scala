package tmc.CrafterTracker.listener

import org.bukkit.event.{EventHandler, Listener}
import tmc.CrafterTracker.services.SessionRepository
import org.bukkit.event.player.{PlayerLoginEvent, PlayerQuitEvent}
import tmc.CrafterTracker.domain.{SessionMap, Session}

// Created by cyrus on 5/1/12 at 2:05 PM

class PlayerConnectionListener(sessionRepository: SessionRepository) extends Listener {
  var this.sessionRepository = sessionRepository

  @EventHandler
  def onPlayerConnect(event: PlayerLoginEvent) {
    val playerName = event.getPlayer.getName
    val ipAddress = event.getAddress.toString
    SessionMap.put(playerName, new Session(playerName, ipAddress))
  }

  @EventHandler
  def onPlayerDisconnect(event: PlayerQuitEvent) {
    SessionMap.applyToSessionFor(event.getPlayer.getName,
      (s:Session) => { s.disconnected; sessionRepository.save(s) })
    SessionMap.remove(event.getPlayer.getName)
  }
}
