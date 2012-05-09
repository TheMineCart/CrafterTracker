package tmc.CrafterTracker.executors

import tmc.BukkitTestUtilities.Services.RepositoryTest
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import tmc.CrafterTracker.services.{WarningMessageRepository, PlayerRepository}
import tmc.BukkitTestUtilities.Mocks.{TestServer, TestPlayer}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import tmc.CrafterTracker.domain.Player
import org.bukkit.ChatColor

// Created by cyrus on 5/9/12 at 1:12 PM

@RunWith(classOf[JUnitRunner])
class WarningExecutorTest extends RepositoryTest with FlatSpec with ShouldMatchers with BeforeAndAfterEach {
  var warningRepository: WarningMessageRepository = null
  var playerRepository: PlayerRepository = null
  var executor: WarningExecutor = null
  var server: TestServer = null
  var nonAdminPlayer: TestPlayer = null
  var adminPlayer: TestPlayer = null

  override def beforeEach() {
    warningRepository = new WarningMessageRepository(getCollection("WarningMessages"))
    playerRepository = new PlayerRepository(getCollection("Players"))
    server = new TestServer
    nonAdminPlayer = new TestPlayer()
    server.addOnlinePlayer(nonAdminPlayer)

    adminPlayer = new TestPlayer()
    adminPlayer.setOp(true)
    server.addOnlinePlayer(adminPlayer)

    executor = new WarningExecutor(server, playerRepository, warningRepository)
  }

  override def afterEach() {
    clearTestData()
  }

  it should "return false if there are an incorrect number of arguments" in {
    var result = executor.onCommand(adminPlayer, null, "warn", Array())
    result should equal (false)

    result = executor.onCommand(adminPlayer, null, "warn", Array("Jason"))
    result should equal (false)

    result = executor.onCommand(adminPlayer, null, "warn", Array("Jason", "minor"))
    result should equal (false)

    result = executor.onCommand(adminPlayer, null, "warn", Array("Jason", "minor", "a message", "way too many arguments"))
    result should equal (false)
  }

  it should "return true if the command sender is not Op and the number of arguments is incorrect" in {
    var result = executor.onCommand(nonAdminPlayer, null, "warn", Array())
    result should equal (true)
  }

  it should "return true if there are the correct number of arguments" in {
    val result = executor.onCommand(adminPlayer, null, "warn", Array("Jason", "minor", "a message"))
    result should equal (true)
  }

  it should "prevent a non admin player from creating a warning" in {
    playerRepository.save(new Player("Jason"))

    val result = executor.onCommand(nonAdminPlayer, null, "warn", Array("Jason", "minor", "You have been a bad player."))

    nonAdminPlayer.getMessage() should equal ("You do not have access to that command!")
    result should equal (true)
    playerRepository.findByPlayerName("Jason").penaltyScore should equal (0)
  }

  it should "validate that the player exists in the database" in {
    val result = executor.onCommand(adminPlayer, null, "warn", Array("Jason", "minor", "You have been a bad player."))

    result should equal (true)
    adminPlayer.getMessage() should equal ("Could not find player " + ChatColor.DARK_PURPLE + "" +
                                           "Jason" + ChatColor.WHITE + ". Please double check your spelling.")
  }

  //The Happy Path
  it should "create a new warning and save it to the database and update the user's penalty score" in {
    val goodJason = new Player("Jason")
    goodJason.addBroken(100)
    goodJason.addPlaced(100)
    goodJason.addMinutesPlayed(60)
    goodJason.calculateScore
    println(goodJason.score)
    playerRepository.save(goodJason)

    warningRepository.findByPlayerName("Jason").size should equal(0)

    val result = executor.onCommand(adminPlayer, null, "warn", Array("Jason", "minor", "You have been a bad player.") )

    result should equal (true)
    warningRepository.findByPlayerName("Jason").size should equal(1)

    val badJason = playerRepository.findByPlayerName("Jason")
    badJason.penaltyScore should equal (1200)
    badJason.score should equal (10800)
  }

}