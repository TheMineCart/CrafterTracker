package tmc.CrafterTracker.listener

import org.bukkit.entity.{Player => BukkitPlayer}
import org.scalatest.matchers.ShouldMatchers
import tmc.BukkitTestUtilities.Mocks.TestPlayer
import tmc.CrafterTracker.domain.Player
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.bukkit.event.player.{PlayerLoginEvent, PlayerQuitEvent}
import java.net.InetAddress
import tmc.CrafterTracker.builders.aSession
import tmc.CrafterTracker.domain.{SessionMap, Session}
import tmc.BukkitTestUtilities.Services.{RepositoryTest, TimeFreezeService}
import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import tmc.CrafterTracker.services.{PlayerRepository, SessionRepository}

// Created by cyrus on 5/1/12 at 3:20 PM

@RunWith(classOf[JUnitRunner])
class PlayerConnectionListenerTest extends RepositoryTest with FlatSpec with ShouldMatchers with BeforeAndAfterEach {
  SessionRepository.collection = getCollection("Sessions")
  var sessionRepository = SessionRepository
  PlayerRepository.collection = getCollection("Players")
  var playerRepository = PlayerRepository
  var playerConnectionListener: PlayerConnectionListener = null
  var jason: BukkitPlayer = null

  override def beforeEach() {
    playerConnectionListener = new PlayerConnectionListener
    jason = new TestPlayer("Jason")
  }

  override def afterEach() {
    clearTestData()
  }

  "The listener" should "add a session to the sessions map when a player joins" in {
    val event = new PlayerLoginEvent(jason, "some host", InetAddress.getLocalHost)
    playerConnectionListener.onPlayerConnect(event)
    SessionMap.sessions.size should equal (1)
  }

  "The listener" should "remove a session from the sessions map when a player disconnects" in {
    SessionMap.put("Jason", aSession.withPlayerName("Jason").build())
    TimeFreezeService.freeze()
    sessionRepository.count should equal (0)

    val event = new PlayerQuitEvent(jason, "I quit.")
    playerConnectionListener.onPlayerDisconnect(event)

    sessionRepository.count should equal (1)

    SessionMap.sessions.size should equal (0)
    val session: Session = sessionRepository.findByPlayerName("Jason").head
    session.disconnectedAt should equal (new DateTime)
    session.ipAddress should equal ("127.0.0.1")
    TimeFreezeService.unfreeze()
  }

  it should "create a new Player object if the player connecting is not in the database" in {
    playerRepository.count() should equal (0)

    val now = new DateTime
    TimeFreezeService.freeze(now)

    val event = new PlayerLoginEvent(jason, "some host", InetAddress.getLocalHost)
    playerConnectionListener.onPlayerConnect(event)

    TimeFreezeService.unfreeze()

    playerRepository.count() should equal (1)
    val newJason = playerRepository.findByPlayerName("Jason")
    newJason.joinedOn should equal (now)
  }

  it should "not overwrite existing player on player login" in {

    val now = new DateTime()
    TimeFreezeService.freeze(now)
    val firstEvent = new PlayerLoginEvent(jason, "some host", InetAddress.getLocalHost)
    playerConnectionListener.onPlayerConnect(firstEvent)
    TimeFreezeService.unfreeze()

    TimeFreezeService.freeze(now.plusDays(1))
    val secondEvent = new PlayerLoginEvent(jason, "some host", InetAddress.getLocalHost)
    playerConnectionListener.onPlayerConnect(secondEvent)
    TimeFreezeService.unfreeze()

    val newJason = playerRepository.findByPlayerName("Jason")
    newJason.joinedOn should equal (now)
  }

  it should "update player statistics on player disconnect" in {
    val now = new DateTime
    TimeFreezeService.freeze(now)
    SessionMap.put("Jason", new Session("Jason", InetAddress.getLocalHost.toString))
    SessionMap.applyToSessionFor("Jason", (session) => { session.blocksBroken = 40; session.blocksPlaced = 90; })
    val player = new Player("Jason")
    player.addPenaltyScore(5000)
    playerRepository.save(player)

    TimeFreezeService.freeze(now.plusHours(1))
    val event = new PlayerQuitEvent(jason, "Leaving the server")
    playerConnectionListener.onPlayerDisconnect(event)
    TimeFreezeService.unfreeze()

    val newJason = playerRepository.findByPlayerName("Jason")
    newJason.minutesPlayed should equal (60)
    newJason.blocksBroken should equal (40)
    newJason.blocksPlaced should equal (90)
    newJason.score should equal (2800)
  }

  it should "not update player statistics if the player does not exist in the database" in {
    SessionMap.put("Jason", new Session("Jason", InetAddress.getLocalHost.toString))

    val event = new PlayerQuitEvent(jason, "Leaving the server")
    playerConnectionListener.onPlayerDisconnect(event)

    playerRepository.exists("Jason") should equal (false)
    SessionMap.get("Jason") should equal (None)
  }
}
