package tmc.CrafterTracker.listener

import org.bukkit.entity.{Player => BukkitPlayer}
import org.scalatest.matchers.ShouldMatchers
import tmc.BukkitTestUtilities.Mocks.TestPlayer
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.net.InetAddress
import tmc.CrafterTracker.builders.aSession
import tmc.BukkitTestUtilities.Services.{RepositoryTest, TimeFreezeService}
import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import tmc.CrafterTracker.services.{WarningMessageRepository, PlayerRepository, SessionRepository}
import tmc.CrafterTracker.domain._
import org.bukkit.ChatColor
import org.bukkit.event.player.{PlayerJoinEvent, PlayerLoginEvent, PlayerQuitEvent}

// Created by cyrus on 5/1/12 at 3:20 PM

@RunWith(classOf[JUnitRunner])
class PlayerConnectionListenerTest extends RepositoryTest with FlatSpec with ShouldMatchers with BeforeAndAfterEach {
  SessionRepository.collection = getCollection("Sessions")
  var sessionRepository = SessionRepository
  PlayerRepository.collection = getCollection("Players")
  var playerRepository = PlayerRepository
  WarningMessageRepository.collection = getCollection("WarningMessages")
  var jason: BukkitPlayer = null

  override def beforeEach() {
    jason = new TestPlayer("Jason")
  }

  override def afterEach() {
    clearTestData()
  }

  it should "add a session to the sessions map when a player joins" in {
    val event = new PlayerLoginEvent(jason, "some host", InetAddress.getLocalHost)
    PlayerConnectionListener.onPlayerConnect(event)
    SessionMap.sessions.size should equal (1)
  }

  it should "remove a session from the sessions map when a player disconnects" in {
    SessionMap.put("Jason", aSession.withPlayerName("Jason").build())
    TimeFreezeService.freeze()
    sessionRepository.count should equal (0)

    val event = new PlayerQuitEvent(jason, "I quit.")
    PlayerConnectionListener.onPlayerDisconnect(event)

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
    PlayerConnectionListener.onPlayerConnect(event)

    TimeFreezeService.unfreeze()

    playerRepository.count() should equal (1)
    val newJason = playerRepository.findByPlayerName("Jason")
    newJason.joinedOn should equal (now)
  }

  it should "not overwrite existing player on player login" in {

    val now = new DateTime()
    TimeFreezeService.freeze(now)
    val firstEvent = new PlayerLoginEvent(jason, "some host", InetAddress.getLocalHost)
    PlayerConnectionListener.onPlayerConnect(firstEvent)
    TimeFreezeService.unfreeze()

    TimeFreezeService.freeze(now.plusDays(1))
    val secondEvent = new PlayerLoginEvent(jason, "some host", InetAddress.getLocalHost)
    PlayerConnectionListener.onPlayerConnect(secondEvent)
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
    PlayerConnectionListener.onPlayerDisconnect(event)
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
    PlayerConnectionListener.onPlayerDisconnect(event)

    playerRepository.exists("Jason") should equal (false)
    SessionMap.get("Jason") should equal (None)
  }

  it should "not show the names of players with new warnings if the player logging in is not a server operator" in {
    val now = new DateTime
    TimeFreezeService.freeze(now)
    val player = new TestPlayer("NonAdmin")

    playerRepository.save(new Player("NonAdmin"))
    val session = new Session("NonAdmin", "127.0.0.1")
    TimeFreezeService.freeze(now.plusMinutes(20))
    session.disconnected
    SessionRepository.save(session)

    TimeFreezeService.freeze(now.plusMinutes(60))
    WarningMessageRepository.save(new WarningMessage("Sam", "Jason", "testing admin notification on login", Minor, 1000))

    TimeFreezeService.freeze(now.plusHours(2))
    val event = new PlayerJoinEvent(player, "Joining the server")
    PlayerConnectionListener.onPlayerJoin(event)

    TimeFreezeService.unfreeze()

    player.getMessage should equal (null)
  }

  it should "Show the names of players with warnings issued since last session when an admin logs in" in {
    val now = new DateTime
    TimeFreezeService.freeze(now)
    val admin = new TestPlayer("Admin")
    admin.setOp(true)

    playerRepository.save(new Player("Admin"))
    val session = new Session("Admin", "127.0.0.1")
    TimeFreezeService.freeze(now.plusMinutes(20))
    session.disconnected
    SessionRepository.save(session)

    TimeFreezeService.freeze(now.plusMinutes(60))
    WarningMessageRepository.save(new WarningMessage("Sam", "Jason", "testing admin notification on login", Minor, 1000))

    TimeFreezeService.freeze(now.plusMinutes(90))
    WarningMessageRepository.save(new WarningMessage("Sam", "Creeper", "creepers be creep'n", Major, 2000))

    TimeFreezeService.freeze(now.plusHours(2))
    val event = new PlayerJoinEvent(admin, "Joining the server")
    PlayerConnectionListener.onPlayerJoin(event)

    TimeFreezeService.unfreeze()

    admin.getMessage(0) should equal (ChatColor.RED + "These players received warnings while you were offline: ")
    admin.getMessage(1) should equal (ChatColor.DARK_PURPLE + "Creeper" + ChatColor.WHITE + ", " + ChatColor.DARK_PURPLE + "Jason")
    admin.getMessage(2) should equal (" ")

    admin.getMessage(3) should equal ("* " + ChatColor.DARK_PURPLE + "Creeper" + ChatColor.WHITE + " received a " + Major.chatOutput +
                                      " warning for \"" + ChatColor.GRAY + "creepers be creep'n" + ChatColor.WHITE + "\" and lost " +
                                      ChatColor.DARK_AQUA + 1000 + ChatColor.WHITE + " points.")
    admin.getMessage(4) should equal (" ")
    admin.getMessage(5) should equal ("* " + ChatColor.DARK_PURPLE + "Jason" + ChatColor.WHITE + " received a " + Minor.chatOutput + " warning for \""
                                      + ChatColor.GRAY + "testing admin notification on login" + ChatColor.WHITE + "\" and lost " +
                                      ChatColor.DARK_AQUA + 100 + ChatColor.WHITE + " points.")
    admin.getMessage(6) should equal (" ")
  }

  it should "not notify the admin on long if there are no new warnings" in {
    val now = new DateTime
    TimeFreezeService.freeze(now)
    val admin = new TestPlayer("Admin")
    admin.setOp(true)

    playerRepository.save(new Player("Admin"))
    val session = new Session("Admin", "127.0.0.1")
    TimeFreezeService.freeze(now.plusMinutes(20))
    session.disconnected
    SessionRepository.save(session)

    val event = new PlayerJoinEvent(admin, "Joining the server")
    PlayerConnectionListener.onPlayerJoin(event)

    admin.getMessages.size() should equal (0)
  }
}
