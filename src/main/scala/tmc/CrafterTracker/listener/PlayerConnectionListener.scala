package tmc.CrafterTracker.listener

import org.bukkit.entity.{Player => BukkitPlayer}
import org.joda.time.Minutes
import tmc.CrafterTracker.domain.{Player, SessionMap, Session}
import tmc.CrafterTracker.services.{WarningMessageRepository, PlayerRepository, SessionRepository}
import org.bukkit.ChatColor.{RED,DARK_PURPLE,WHITE,DARK_AQUA,GRAY}
import collection.mutable.HashSet
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.event.player.{PlayerJoinEvent, PlayerLoginEvent, PlayerQuitEvent}

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
  def onPlayerJoin(event: PlayerJoinEvent) {
    if (event.getPlayer.isOp)
      displayRecipientsWithNewWarnings(event.getPlayer)
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

  private def displayRecipientsWithNewWarnings(serverOperator: BukkitPlayer) {
    val session = SessionRepository.findMostRecentByPlayerName(serverOperator.getName)
    val recentWarnings = WarningMessageRepository.findMessagesSince(session.disconnectedAt)
    if (recentWarnings.size > 0) {
      serverOperator.sendMessage(RED + "These players received warnings while you were offline: ")

      var players = new HashSet[String]
      recentWarnings.foreach(warning => players += warning.recipient)
      val warnedPlayers: String = DARK_PURPLE + players.mkString(WHITE + ", " + DARK_PURPLE)

      serverOperator.sendMessage(warnedPlayers)
      serverOperator.sendMessage(" ")
      recentWarnings.foreach(warning => {
        serverOperator.sendMessage("* " + DARK_PURPLE + warning.recipient +
          WHITE + " received a " + warning.infraction.chatOutput + " warning for \"" + GRAY +
          warning.text + WHITE + "\" and lost " + DARK_AQUA + warning.score + WHITE + " points.")
        serverOperator.sendMessage(" ")
      })
    }
  }
}
