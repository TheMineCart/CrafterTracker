package tmc.CrafterTracker

import domain.{Session, Player, SessionMap}
import services.{PlayerRepository, SessionRepository}
import tmc.BukkitTestUtilities.Mocks.{TestPlayer, TestServer}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.joda.time.DateTime
import tmc.BukkitTestUtilities.Services.{RepositoryTest, TimeFreezeService}

// Created by cyrus on 5/4/12 at 10:21 AM

@RunWith(classOf[JUnitRunner])
class CrafterTrackerPluginTest extends RepositoryTest with FlatSpec with ShouldMatchers with BeforeAndAfterEach {
  val plugin = new CrafterTrackerPlugin
  var testServer: TestServer = null

  SessionRepository.collection = getCollection("Sessions")
  PlayerRepository.collection = getCollection("Players")

  override def beforeEach() {
    testServer = new TestServer
    CtPlugin.server = testServer
  }

  override def afterEach() {
    SessionMap.clear()
    clearTestData()
  }

  it should "Create a session for each player currently connected when the plugin is reloaded" in {
    testServer.addOnlinePlayer(new TestPlayer("Jason"))
    testServer.addOnlinePlayer(new TestPlayer("Sam"))

    TimeFreezeService.freeze()
    plugin.setUpSessions()

    SessionMap.sessions.size should equal (2)
    SessionMap.get("Sam").get.connectedAt should equal (new DateTime)
    TimeFreezeService.unfreeze()
  }
  
  it should "persist players if it reloads while there are connected players who have not been persisted" in {
    PlayerRepository.count should equal (0)
    testServer.addOnlinePlayer(new TestPlayer("Jason"))
    testServer.addOnlinePlayer(new TestPlayer("Sam"))

    TimeFreezeService.freeze()
    plugin.setUpSessions()

    PlayerRepository.count should equal (2)
    PlayerRepository.findByPlayerName("Sam").joinedOn should equal (new DateTime)
    PlayerRepository.findByPlayerName("Jason").joinedOn should equal (new DateTime)

    TimeFreezeService.unfreeze()
  }

  it should "Not create any sessions if there are no currently online players when plugin loads" in {
    plugin.setUpSessions()

    SessionMap.sessions.size should equal (0)
  }

  it should "save any exisiting sessions and clear the session map when plugin is disabled" in {
    testServer.addOnlinePlayer(new TestPlayer("Sam"))
    PlayerRepository.save(new Player("Sam"))
    SessionMap.put("Sam", new Session("Sam", "127.0.0.1"))

    testServer.addOnlinePlayer(new TestPlayer("Jason"))
    PlayerRepository.save(new Player("Jason"))
    SessionMap.put("Jason", new Session("Jason", "127.0.0.1"))

    TimeFreezeService.freeze()
    plugin.tearDownSessions()

    SessionMap.sessions.size should equal (0)
    SessionRepository.count should equal (2)
    SessionRepository.findByPlayerName("Sam").head.disconnectedAt should equal (new DateTime)
    TimeFreezeService.unfreeze()
  }

  it should "update a player's statistics if the plugin is disabled" in {
    val now = DateTime.parse("2012-04-26T12:00:00.000-04:00")
    TimeFreezeService.freeze(now)

    PlayerRepository.save(new Player("Sam"))
    PlayerRepository.save(new Player("Jason"))

    testServer.addOnlinePlayer(new TestPlayer("Sam"))
    val samSession = new Session("Sam", "127.0.0.1")
    samSession.blocksBroken = 100
    samSession.blocksPlaced = 200
    SessionMap.put("Sam", samSession)

    testServer.addOnlinePlayer(new TestPlayer("Jason"))
    val jasonSession = new Session("Jason", "127.0.0.1")
    jasonSession.blocksBroken = 200
    jasonSession.blocksPlaced = 300
    SessionMap.put("Jason", jasonSession)


    TimeFreezeService.freeze(now.plusMinutes(120))
    plugin.tearDownSessions()

    val sam = PlayerRepository.findByPlayerName("Sam")
    sam.blocksBroken should equal (100)
    sam.blocksPlaced should equal (200)
    sam.score should equal (36000)

    val jason = PlayerRepository.findByPlayerName("Jason")
    jason.blocksBroken should equal (200)
    jason.blocksPlaced should equal (300)
    jason.score should equal (60000)

    TimeFreezeService.unfreeze()
  }

  it should "not save any sessions if there are no online players when the plugin is disabled" in {
    plugin.tearDownSessions()

    SessionMap.sessions.size should equal (0)
    SessionRepository.count should equal (0)
  }
}
