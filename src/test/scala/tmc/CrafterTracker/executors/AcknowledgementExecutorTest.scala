package tmc.CrafterTracker.executors

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import tmc.CrafterTracker.services.{PlayerRepository, WarningMessageRepository}
import tmc.BukkitTestUtilities.Mocks.TestPlayer
import org.bukkit.ChatColor
import tmc.BukkitTestUtilities.Services.{TimeFreezeService, RepositoryTest}
import org.joda.time.DateTime
import tmc.CrafterTracker.domain.{Major, WarningMessage}
import tmc.CrafterTracker.CtPlugin

// Created by cyrus on 5/15/12 at 9:44 AM

class AcknowledgementExecutorTest extends RepositoryTest with FlatSpec with ShouldMatchers with BeforeAndAfterEach {
  var sam: TestPlayer = null

  override def beforeEach() {
    WarningMessageRepository.collection = getCollection("WarningMessages")
    PlayerRepository.collection = getCollection("Players")
    sam = new TestPlayer("Sam")
  }

  override def afterEach() {
    clearTestData()
  }

  it should "return false if there are an incorrect number of arguments" in {
    var result = AcknowledgementExecutor.onCommand(sam, null, "acknowledge", Array())
    result should equal (false)

    result = AcknowledgementExecutor.onCommand(sam, null, "acknowledge", Array("1", "Too many"))
    result should equal (false)
  }

  it should "return true if there is one parameter" in {
    val result = AcknowledgementExecutor.onCommand(sam, null, "acknowledge", Array("123"))
    result should equal (true)
  }

  it should "return true and send message to player if argument is not an integer" in {
    val result = AcknowledgementExecutor.onCommand(sam, null, "acknowledge", Array("junk"))
    result should equal (true)
    sam.getMessage should equal (ChatColor.DARK_RED + "Incorrect parameter, must be a number " +
                                 "matching a warning that you have receieved.")
  }

  it should "not acknowledge a message that the command sender does not own" in {
    val now = new DateTime()
    TimeFreezeService.freeze(now)
    WarningMessageRepository.save(new WarningMessage("Sam", "Jason", "You bad", Major, 1000))
    val result = AcknowledgementExecutor.onCommand(sam, null, "acknowledge",
                 Array(now.toString(CtPlugin.warningIdFormat)))

    result should equal (true)
    sam.getMessage() should equal (ChatColor.DARK_RED + "Incorrect parameter, no warnings match #" +
                                   now.toString(CtPlugin.warningIdFormat) )
    WarningMessageRepository.findByPlayerName("Jason")(0).acknowledged should equal (false)
    TimeFreezeService.unfreeze()
  }

  it should "return true and notify sender if the message has been successfully acknowledged" in {
    val now = new DateTime(2012, 05, 15, 15, 05, 20, 31)
    TimeFreezeService.freeze(now)
    WarningMessageRepository.save(new WarningMessage("Sam", "Jason", "You bad", Major, 1000))
    val jason = new TestPlayer("Jason")
    val result = AcknowledgementExecutor.onCommand(jason, null, "acknowledge", Array("1205151505"))

    result should equal (true)
    jason.getMessage() should equal (ChatColor.DARK_GREEN + "You have successfully acknowledged warning #" +
                                     now.toString(CtPlugin.warningIdFormat))
    WarningMessageRepository.findByPlayerName("Jason")(0).acknowledged should equal (true)
    TimeFreezeService.unfreeze()
  }

}
