package tmc.CrafterTracker.listener

import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.Server
import collection.immutable.HashMap
import tmc.CrafterTracker.services.SessionRepository
import org.bukkit.event.player.{PlayerLoginEvent, PlayerQuitEvent, PlayerJoinEvent}
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
    var session: Session = SessionMap.get(event.getPlayer.getName)
    session.disconnected
    sessionRepository.save(session)
    SessionMap.remove(event.getPlayer.getName)
  }
}
