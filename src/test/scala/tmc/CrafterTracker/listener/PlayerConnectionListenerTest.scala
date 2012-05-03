package tmc.CrafterTracker.listener

import org.scalatest.FlatSpec
import org.bukkit.entity.Player
import org.scalatest.matchers.ShouldMatchers
import tmc.BukkitTestUtilities.Mocks.{TestServer, TestPlayer}
import org.bukkit.event.player.{PlayerQuitEvent, PlayerJoinEvent}
import tmc.CrafterTracker.domain.Session
import org.joda.time.DateTime
import tmc.BukkitTestUtilities.Services.TimeFreezeService
import tmc.CrafterTracker.services.SessionRepository
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

// Created by cyrus on 5/1/12 at 3:20 PM

@RunWith(classOf[JUnitRunner])
class PlayerConnectionListenerTest extends FlatSpec with ShouldMatchers {
  var sessionRepository = new SessionRepository
  var playerConnectionListener = new PlayerConnectionListener(new TestServer, sessionRepository)
  var jason = new TestPlayer("Jason")

  "The listener" should "add a session to the sessions map when a player joins" in {
    val event = new PlayerJoinEvent(jason, "Rawr")
    playerConnectionListener.onPlayerJoin(event)
    playerConnectionListener.sessionsMap.size should equal (1)
  }

  "The listener" should "remove a session from the sessions map when a player disconnects" in {
    playerConnectionListener.sessionsMap += "Jason" -> new Session("Jason")
    TimeFreezeService.freeze()
    sessionRepository.sessions.size should equal (0)

    val event = new PlayerQuitEvent(jason, "I quit.")
    playerConnectionListener.onPlayerLeave(event)

    sessionRepository.sessions.size should equal (1)
    playerConnectionListener.sessionsMap.size should equal (0)
    sessionRepository.findByPlayerName("Jason").last.disconnectedAt should equal (new DateTime)
    TimeFreezeService.unfreeze()
  }
}
