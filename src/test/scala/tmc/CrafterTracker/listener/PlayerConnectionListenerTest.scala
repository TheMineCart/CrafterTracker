package tmc.CrafterTracker.listener

import org.scalatest.FlatSpec
import org.bukkit.entity.Player
import org.scalatest.matchers.ShouldMatchers
import tmc.BukkitTestUtilities.Mocks.{TestServer, TestPlayer}
import tmc.CrafterTracker.domain.Session
import org.joda.time.DateTime
import tmc.BukkitTestUtilities.Services.TimeFreezeService
import tmc.CrafterTracker.services.SessionRepository
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.bukkit.event.player.{PlayerLoginEvent, PlayerQuitEvent, PlayerJoinEvent}
import java.net.InetAddress
import tmc.CrafterTracker.builders.{aSession}

// Created by cyrus on 5/1/12 at 3:20 PM

@RunWith(classOf[JUnitRunner])
class PlayerConnectionListenerTest extends FlatSpec with ShouldMatchers {
  var sessionRepository = new SessionRepository
  var playerConnectionListener = new PlayerConnectionListener(new TestServer, sessionRepository)
  var jason = new TestPlayer("Jason")

  "The listener" should "add a session to the sessions map when a player joins" in {
    val event = new PlayerLoginEvent(jason, "some host", InetAddress.getLocalHost)
    playerConnectionListener.onPlayerConnect(event)
    playerConnectionListener.sessionsMap.size should equal (1)
  }

  "The listener" should "remove a session from the sessions map when a player disconnects" in {
    playerConnectionListener.sessionsMap += "Jason" -> aSession.withPlayerName("Jason").build
    TimeFreezeService.freeze()
    sessionRepository.sessions.size should equal (0)

    val event = new PlayerQuitEvent(jason, "I quit.")
    playerConnectionListener.onPlayerDisconnect(event)

    sessionRepository.sessions.size should equal (1)
    playerConnectionListener.sessionsMap.size should equal (0)
    sessionRepository.findByPlayerName("Jason").last.disconnectedAt should equal (new DateTime)
    TimeFreezeService.unfreeze()
  }
}
