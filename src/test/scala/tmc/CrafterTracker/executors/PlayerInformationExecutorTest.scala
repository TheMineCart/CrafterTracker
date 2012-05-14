package tmc.CrafterTracker.executors

import tmc.BukkitTestUtilities.Mocks.TestPlayer
import org.scalatest.matchers.ShouldMatchers
import tmc.CrafterTracker.services.PlayerRepository
import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import tmc.CrafterTracker.domain.Player
import tmc.BukkitTestUtilities.Services.{TimeFreezeService, RepositoryTest}
import org.joda.time.DateTime
import org.bukkit.ChatColor
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

// Created by cyrus on 5/14/12 at 10:00 AM

@RunWith(classOf[JUnitRunner])
class PlayerInformationExecutorTest extends RepositoryTest with FlatSpec with ShouldMatchers with BeforeAndAfterEach {
  var panda: Player = null
  var admin: TestPlayer = null
  val thirteenthOfMay = new DateTime(2012, 5, 13, 0, 0, 0)
  val fourteenthOfMay = new DateTime(2012, 5, 14, 0, 0, 0)

  override def beforeEach() {
    TimeFreezeService.freeze(fourteenthOfMay)
    panda = new Player("Panda")
    admin = new TestPlayer("Panda")
    admin.setOp(true)

    PlayerRepository.collection = getCollection("Players")
    PlayerRepository.save(panda)
  }

  override def afterEach() {
    TimeFreezeService.unfreeze()
    clearTestData()
  }

  it should "return false if there are too many arguments" in {
    val result = PlayerInformationExecutor.onCommand(new TestPlayer("Panda"), null, "playerinfo", Array("1", "too many"))
    result should equal (false)
  }

  it should "return true and print command access error if player is not op and looking at other player info" in {
    val nonAdmin = new TestPlayer("NonAdminPlayer")

    val result = PlayerInformationExecutor.onCommand(nonAdmin, null, "playerinfo", Array("Sam"))

    result should equal (true)
    nonAdmin.getMessage should equal ("You do not have access to that command!")
  }

  it should "print an error message and return true if the player does not exist" in {

    val result = PlayerInformationExecutor.onCommand(admin, null, "playerinfo", Array("Sam"))
    result should equal (true)
    admin.getMessage should equal ("Player " + ChatColor.DARK_PURPLE + "Sam" + ChatColor.WHITE + " does not exist. Please check your spelling.")
  }

  it should "show you your own information if you don't specify any arguments" in {
    panda.minutesPlayed = 120
    PlayerRepository.save(panda)
    val result = PlayerInformationExecutor.onCommand(admin, null, "playerinfo", Array())

    admin.getMessage should equal ("Player " + ChatColor.DARK_PURPLE + "Panda" + ChatColor.WHITE +
                                   " joined the server on " + ChatColor.DARK_PURPLE + "5/14/2012" + ChatColor.WHITE +
                                   " and has played an average of " + ChatColor.DARK_PURPLE + "120" + ChatColor.WHITE +
                                   " minutes per day.")
    result should equal (true)
  }

  it should "show a player's information if player exists" in {
    TimeFreezeService.freeze(thirteenthOfMay)
    val sam = new Player("Sam")
    sam.minutesPlayed = 120
    PlayerRepository.save(sam)

    TimeFreezeService.freeze(fourteenthOfMay)
    val result = PlayerInformationExecutor.onCommand(admin, null, "playerinfo", Array("Sam"))

    admin.getMessage should equal ("Player " + ChatColor.DARK_PURPLE + "Sam" + ChatColor.WHITE +
                                   " joined the server on " + ChatColor.DARK_PURPLE + "5/13/2012" + ChatColor.WHITE +
                                   " and has played an average of " + ChatColor.DARK_PURPLE + "60" + ChatColor.WHITE +
                                   " minutes per day.")
    result should equal (true)
  }

}
