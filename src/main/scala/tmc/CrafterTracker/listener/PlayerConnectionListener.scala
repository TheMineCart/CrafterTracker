package tmc.CrafterTracker.listener

import org.bukkit.event.{EventHandler, Listener}
import tmc.CrafterTracker.domain.Session
import org.bukkit.Server
import collection.immutable.HashMap
import tmc.CrafterTracker.services.SessionRepository
import org.bukkit.event.player.{PlayerLoginEvent, PlayerQuitEvent, PlayerJoinEvent}

// Created by cyrus on 5/1/12 at 2:05 PM

class PlayerConnectionListener(server: Server, sessionRepository: SessionRepository) extends Listener {
  var sessionsMap: Map[String, Session] = new HashMap[String, Session]
  var this.server: Server = server
  var this.sessionRepository = sessionRepository

  @EventHandler
  def onPlayerConnect(event: PlayerLoginEvent) {
    val playerName = event.getPlayer.getName
    val ipAddress = event.getAddress.toString
    sessionsMap += playerName -> new Session(playerName, ipAddress)
  }

  @EventHandler
  def onPlayerDisconnect(event: PlayerQuitEvent) {
    var session: Session = sessionsMap.get(event.getPlayer.getName).get
    session.disconnected
    sessionRepository.save(session)
    sessionsMap -= event.getPlayer.getName
  }
}
