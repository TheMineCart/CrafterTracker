package tmc.CrafterTracker

import tmc.BukkitTestUtilities.Mocks.{TestPlayer, TestServer}
import domain.{Session, SessionMap}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import services.SessionRepository
import org.joda.time.DateTime
import tmc.BukkitTestUtilities.Services.{RepositoryTest, TimeFreezeService}

// Created by cyrus on 5/4/12 at 10:21 AM
@RunWith(classOf[JUnitRunner])
class CrafterTrackerPluginTest extends RepositoryTest with FlatSpec with ShouldMatchers with BeforeAndAfterEach {
  var plugin: CrafterTrackerPlugin = null
  var testServer: TestServer = null

  override def beforeEach() {
    plugin = new CrafterTrackerPlugin
    testServer = new TestServer
    plugin.server = testServer
    plugin.sessionRepository = new SessionRepository(getCollection("Sessions"))
  }

  override def afterEach() {
    SessionMap.clear()
    clearTestData()
  }

  "The plugin" should "Create a session for each player currently connected when the plugin is reloaded" in {
    testServer.addOnlinePlayer(new TestPlayer("Jason"))
    testServer.addOnlinePlayer(new TestPlayer("Sam"))

    TimeFreezeService.freeze()
    plugin.setupSessionMap()

    SessionMap.sessions.size should equal (2)
    SessionMap.get("Sam").connectedAt should equal (new DateTime)
    TimeFreezeService.unfreeze()
  }

  "The plugin" should "Not create any sessions if there are no currently online players when plugin loads" in {
    plugin.setupSessionMap()

    SessionMap.sessions.size should equal (0)
  }

  "The plugin" should "save any exisiting sessions and clear the session map when plugin is disabled" in {
    testServer.addOnlinePlayer(new TestPlayer("Sam"))
    SessionMap.put("Sam", new Session("Sam", "127.0.0.1"))

    testServer.addOnlinePlayer(new TestPlayer("Jason"))
    SessionMap.put("Jason", new Session("Jason", "127.0.0.1"))

    TimeFreezeService.freeze()
    plugin.tearDownSessionMap()

    SessionMap.sessions.size should equal (0)
    plugin.sessionRepository.count should equal (2)
    plugin.sessionRepository.findByPlayerName("Sam").head.disconnectedAt should equal (new DateTime)
    TimeFreezeService.unfreeze()
  }

  "The plugin" should "not save any sessions if there are no online players when the plugin is disabled" in {
    plugin.tearDownSessionMap()

    SessionMap.sessions.size should equal (0)
    plugin.sessionRepository.count should equal (0)
  }
}
