package tmc.CrafterTracker.executors

import tmc.BukkitTestUtilities.Services.RepositoryTest
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import tmc.BukkitTestUtilities.Mocks.TestPlayer
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import tmc.CrafterTracker.services.{PlayerRepository, WarningMessageRepository}
import org.bukkit.ChatColor
import tmc.CrafterTracker.domain._

// Created by cyrus on 5/14/12 at 11:27 AM

@RunWith(classOf[JUnitRunner])
class WarningInformationExecutorTest extends RepositoryTest with FlatSpec with ShouldMatchers with BeforeAndAfterEach {

  var admin = new TestPlayer("Gorilla")
  WarningMessageRepository.collection = getCollection("WarningMessages")
  PlayerRepository.collection = getCollection("Players")

  override def beforeEach() {
    admin = new TestPlayer("Gorilla")
    admin.setOp(true)
  }

  override def afterEach() {
    clearTestData()
  }

  it should "return false if there are not enough arguments" in {
    val result = WarningInformationExecutor.onCommand(admin, null, "warningsfor", Array())

    result should equal (false)
  }

  it should "return true and send an access denied message if sender is not server operator" in {
    val nonOpPlayer = new TestPlayer("nonOp")
    val result = WarningInformationExecutor.onCommand(nonOpPlayer, null, "warningsfor", Array("Gorilla"))

    result should equal (true)
    nonOpPlayer.getMessage should equal ("You do not have access to that command!")
  }

  it should "send player does not exist message if the player is not in the repository" in {
    val result = WarningInformationExecutor.onCommand(admin, null, "warningsfor", Array("Gorilla"))
    result should equal (true)
    admin.getMessage should equal ("Player " + ChatColor.DARK_PURPLE + "Gorilla" + ChatColor.WHITE +
                                   " does not exist. Please check your spelling.")
  }

  it should "print a summary of the warning messages given to the player" in {
    PlayerRepository.save(new Player("Penguin"))
    WarningMessageRepository.save(new WarningMessage("Gorilla", "Penguin", "You have been bad", Minor, 120))

    val result = WarningInformationExecutor.onCommand(admin, null, "warningsfor", Array("Penguin"))
    result should equal (true)
    admin.getMessage should equal ("Player " + ChatColor.DARK_PURPLE + "Penguin" + ChatColor.WHITE +
      " has received " + ChatColor.DARK_PURPLE + "1" + ChatColor.WHITE + " warning messages.")
  }

  it should "print a list of the warning messages given to the player" in {
    PlayerRepository.save(new Player("Penguin"))
    generateWarnings(3, "Gorilla", "Penguin")
    val result = WarningInformationExecutor.onCommand(admin, null, "warningsfor", Array("Penguin"))
    result should equal (true)

    admin.getMessages.size() should equal (4)
    admin.getMessage should equal ("Player " + ChatColor.DARK_PURPLE + "Penguin" + ChatColor.WHITE +
      " has received " + ChatColor.DARK_PURPLE + "3" + ChatColor.WHITE + " warning messages.")

    admin.getMessage(1) should equal (Moderate.chatOutput + " infraction with a penalty of " +
                                      ChatColor.DARK_AQUA + "75" + ChatColor.WHITE + " points for \"" + ChatColor.GRAY + "3 falaffel" + ChatColor.WHITE + "\"")
    admin.getMessage(2) should equal (Moderate.chatOutput + " infraction with a penalty of " +
                                      ChatColor.DARK_AQUA + "50" + ChatColor.WHITE + " points for \"" + ChatColor.GRAY + "2 falaffel" + ChatColor.WHITE + "\"")
    admin.getMessage(3) should equal (Moderate.chatOutput + " infraction with a penalty of " +
                                      ChatColor.DARK_AQUA + "25" + ChatColor.WHITE + " points for \"" + ChatColor.GRAY + "1 falaffel" + ChatColor.WHITE + "\"")
  }

  it should "limit the number of warning messages displayed to five per page" in {
    PlayerRepository.save(new Player("Cardinal"))

    generateWarnings(6, "Bluejay", "Cardinal")
    WarningInformationExecutor.onCommand(admin, null, "warningsfor", Array("Cardinal"))

    admin.getMessages.size() should equal (7)

    admin.getMessage(1) should equal (Moderate.chatOutput + " infraction with a penalty of " +
      ChatColor.DARK_AQUA + "150" + ChatColor.WHITE + " points for \"" + ChatColor.GRAY + "6 falaffel" + ChatColor.WHITE + "\"")
    admin.getMessage(2) should equal (Moderate.chatOutput + " infraction with a penalty of " +
      ChatColor.DARK_AQUA + "125" + ChatColor.WHITE + " points for \"" + ChatColor.GRAY + "5 falaffel" + ChatColor.WHITE + "\"")
    admin.getMessage(3) should equal (Moderate.chatOutput + " infraction with a penalty of " +
      ChatColor.DARK_AQUA + "100" + ChatColor.WHITE + " points for \"" + ChatColor.GRAY + "4 falaffel" + ChatColor.WHITE + "\"")
    admin.getMessage(4) should equal (Moderate.chatOutput + " infraction with a penalty of " +
      ChatColor.DARK_AQUA + "75" + ChatColor.WHITE + " points for \"" + ChatColor.GRAY + "3 falaffel" + ChatColor.WHITE + "\"")
    admin.getMessage(5) should equal (Moderate.chatOutput + " infraction with a penalty of " +
      ChatColor.DARK_AQUA + "50" + ChatColor.WHITE + " points for \"" + ChatColor.GRAY + "2 falaffel" + ChatColor.WHITE + "\"")
    admin.getMessage(6) should equal ("See page " + ChatColor.RED + 2 + ChatColor.WHITE + " by executing " + ChatColor.RED + "/wf Cardinal 2")
  }

  it should "display the messages on page 2" in {
    PlayerRepository.save(new Player("Cardinal"))
    generateWarnings(8, "Bluejay", "Cardinal")
    WarningInformationExecutor.onCommand(admin, null, "warningsfor", Array("Cardinal", "2"))

    admin.getMessages.size() should equal (4)

    admin.getMessage(0) should equal ("Page " + ChatColor.RED + 2 + ChatColor.WHITE + " of " + ChatColor.RED + 2)

    admin.getMessage(3) should equal (Moderate.chatOutput + " infraction with a penalty of " +
      ChatColor.DARK_AQUA + "25" + ChatColor.WHITE + " points for \"" + ChatColor.GRAY + "1 falaffel" + ChatColor.WHITE + "\"")
  }

  it should "display a message if your page number exceeds the bounds" in {
    PlayerRepository.save(new Player("Cardinal"))
    WarningInformationExecutor.onCommand(admin, null, "warningsfor", Array("Cardinal", "2"))

    admin.getMessages.size() should equal (1)

    admin.getMessage(0) should equal (ChatColor.DARK_RED + "Sorry but there is no page 2")
  }

  private def generateWarnings (number : Int, senderName : String, recipientName : String) {
    (1 until (number + 1)).foreach(x => WarningMessageRepository.save(new WarningMessage(senderName, recipientName, x + " falaffel", Moderate, x * 100)))
  }
}
