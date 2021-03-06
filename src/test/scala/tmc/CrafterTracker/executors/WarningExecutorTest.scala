package tmc.CrafterTracker.executors

import tmc.BukkitTestUtilities.Services.RepositoryTest
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import tmc.CrafterTracker.services.{WarningMessageRepository, PlayerRepository}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import tmc.CrafterTracker.domain.Player
import org.bukkit.ChatColor
import tmc.CrafterTracker.CtPlugin
import tmc.BukkitTestUtilities.Mocks.{TestOfflinePlayer, TestServer, TestPlayer}

// Created by cyrus on 5/9/12 at 1:12 PM

@RunWith(classOf[JUnitRunner])
class WarningExecutorTest extends RepositoryTest with FlatSpec with ShouldMatchers with BeforeAndAfterEach {
  WarningMessageRepository.collection = getCollection("WarningMessages")
  val warningRepository = WarningMessageRepository

  PlayerRepository.collection = getCollection("Players")
  val playerRepository = PlayerRepository

  var server: TestServer = null
  var nonAdminPlayer: TestPlayer = null
  var adminPlayer: TestPlayer = null

  override def beforeEach() {
    server = new TestServer
    CtPlugin.server = server
    nonAdminPlayer = new TestPlayer()
    server.addOnlinePlayer(nonAdminPlayer)

    adminPlayer = new TestPlayer()
    adminPlayer.setOp(true)
    server.addOnlinePlayer(adminPlayer)
  }

  override def afterEach() {
    clearTestData()
  }

  it should "return false if there are an incorrect number of arguments" in {
    var result = WarningExecutor.onCommand(adminPlayer, null, "warn", Array())
    result should equal (false)

    result = WarningExecutor.onCommand(adminPlayer, null, "warn", Array("Jason"))
    result should equal (false)

    result = WarningExecutor.onCommand(adminPlayer, null, "warn", Array("Jason", "minor"))
    result should equal (false)
  }

  it should "return true if the command sender is not Op and the number of arguments is incorrect" in {
    var result = WarningExecutor.onCommand(nonAdminPlayer, null, "warn", Array())
    result should equal (true)
  }

  it should "return true if there are the correct number of arguments" in {
    playerRepository.save(new Player("Sam"))
    playerRepository.save(new Player("Jason"))

    var result = WarningExecutor.onCommand(adminPlayer, null, "warn", Array("Sam", "minor", "a message"))
    result should equal (true)

    result = WarningExecutor.onCommand(adminPlayer, null, "warn", Array("Jason", "minor", "This", "message", "makes", "more", "than", "3", "arguments."))
    result should equal (true)
    warningRepository.findByPlayerName("Jason")(0).text should equal ("This message makes more than 3 arguments.")
  }

  it should "prevent a non admin player from creating a warning" in {
    playerRepository.save(new Player("Jason"))

    val result = WarningExecutor.onCommand(nonAdminPlayer, null, "warn", Array("Jason", "minor", "You have been a bad player."))

    nonAdminPlayer.getMessage() should equal ("You do not have access to that command!")
    result should equal (true)
    playerRepository.findByPlayerName("Jason").penaltyScore should equal (0)
  }

  it should "validate that the player exists in the database" in {
    val result = WarningExecutor.onCommand(adminPlayer, null, "warn", Array("Jason", "minor", "You have been a bad player."))

    result should equal (true)
    adminPlayer.getMessage() should equal ("Could not find player " + ChatColor.DARK_PURPLE + "" +
                                           "Jason" + ChatColor.WHITE + ". Please double check your spelling.")
  }

  it should "not create a new warning message if the second parameter does not match any Infractions" in {
    playerRepository.save(new Player("Jason"))
    val result = WarningExecutor.onCommand(adminPlayer, null, "warn", Array("Jason", "junk", "You have been a bad player."))

    result should equal (true)
    adminPlayer.getMessage() should equal ("No matching infraction for " + ChatColor.DARK_PURPLE + "" +
      "junk" + ChatColor.WHITE + ". Please double check your spelling.")
  }

  it should "prevent a user from warning another player who is a server operator" in {
    PlayerRepository.save(new Player("Jason"))
    val jason = new TestOfflinePlayer("Jason")
    jason.setOp(true)
    server.addOfflinePlayer(jason)

    val result = WarningExecutor.onCommand(adminPlayer, null, "warn", Array("Jason", "minor", "You will be punished"))

    result should equal (true)
    adminPlayer.getMessage should equal (ChatColor.DARK_RED + "You cannot warn another server operator!")
  }

  //The Happy Path
  it should "create a new warning and save it to the database and update the user's penalty score if the user has a score of 0" in {
    val goodJason = new Player("Jason")
    playerRepository.save(goodJason)

    warningRepository.findByPlayerName("Jason").size should equal(0)

    val result = WarningExecutor.onCommand(adminPlayer, null, "warn", Array("Jason", "minor", "You have been a bad player.") )

    result should equal (true)
    warningRepository.findByPlayerName("Jason").size should equal(1)

    val badJason = playerRepository.findByPlayerName("Jason")
    badJason.penaltyScore should equal (0)
    badJason.score should equal (0)
    adminPlayer.getMessage should equal ("Successfully sent warning to " + ChatColor.DARK_PURPLE + "Jason" + ChatColor.WHITE + ".")
  }

  //The Happy Path
  it should "create a new warning and save it to the database and update the user's penalty score" in {
    val goodJason = new Player("Jason")
    goodJason.addBroken(100)
    goodJason.addPlaced(100)
    goodJason.addMinutesPlayed(60)
    goodJason.calculateScore
    playerRepository.save(goodJason)

    warningRepository.findByPlayerName("Jason").size should equal(0)

    val result = WarningExecutor.onCommand(adminPlayer, null, "warn", Array("Jason", "minor", "You have been a bad player.") )

    result should equal (true)
    warningRepository.findByPlayerName("Jason").size should equal(1)

    val badJason = playerRepository.findByPlayerName("Jason")
    badJason.penaltyScore should equal (1200)
    badJason.score should equal (10800)
    adminPlayer.getMessage should equal ("Successfully sent warning to " + ChatColor.DARK_PURPLE + "Jason" + ChatColor.WHITE + ".")
  }

}
