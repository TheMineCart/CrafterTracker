package tmc.CrafterTracker.services

import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import tmc.BukkitTestUtilities.Services.RepositoryTest
import tmc.CrafterTracker.domain.{Major, WarningMessage}
import tmc.CrafterTracker.CtPlugin
import tmc.BukkitTestUtilities.Mocks.{TestServer, TestPlayer}
import org.bukkit.ChatColor


// Created by cyrus on 5/14/12 at 4:31 PM

class PlayerWarningServiceTest extends RepositoryTest with FlatSpec with ShouldMatchers with BeforeAndAfterEach {
  var server: TestServer = null

  override def beforeEach( ) {
    WarningMessageRepository.collection = getCollection("WarningMessages")
    server = new TestServer()
    CtPlugin.server = server
  }

  override def afterEach() {
    clearTestData()
  }

  it should "display a warning message to a player with an unacknowledged warning" in {
    val jason = new TestPlayer("Jason")
    server.addOnlinePlayer(jason)
    CtPlugin.server = server

    val warning = new WarningMessage("Grizzly", "Jason", "try to be less of a beardon...", Major, 1000)
    WarningMessageRepository.save(warning)

    PlayerWarningService.sleepPeriod = 501
    PlayerWarningService.active = true
    PlayerWarningService.start()
    Thread.sleep(1000)
    PlayerWarningService.active = false

    jason.getMessages.size() should equal(3)
    jason.getMessage should equal ("You have been warned for " + ChatColor.RED + "\"try to be less of a beardon...\"")
    jason.getMessage(1) should equal ("This is a " + Major.chatOutput + " infraction and you have lost " +
                                      ChatColor.DARK_PURPLE + 500 + ChatColor.WHITE + " points")
    jason.getMessage(2) should equal ("In order to stop this message from repeating, please execute /acknowledgewarning 1")
  }

}
