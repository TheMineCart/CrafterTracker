package scala.tmc.CrafterTracker.executors

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import tmc.CrafterTracker.services.PlayerRepository
import org.joda.time.DateTime
import tmc.CrafterTracker.domain.{Session, SessionMap, Player}
import tmc.CrafterTracker.executors.PlayerScoreExecutor
import tmc.BukkitTestUtilities.Mocks.TestPlayer
import org.bukkit.ChatColor.{DARK_AQUA, DARK_PURPLE, WHITE}
import tmc.BukkitTestUtilities.Services.{TimeFreezeService, RepositoryTest}

// Created by cyrus on 5/18/12 at 11:16 AM

class PlayerScoreExecutorTest extends RepositoryTest with FlatSpec with ShouldMatchers with BeforeAndAfterEach {

  PlayerRepository.collection = getCollection("Players")
  var offlinePlayer = new Player("OfflinePlayer")
  var onlinePlayer = new Player("OnlinePlayer")

  override def beforeEach() {
    offlinePlayer = new Player("OfflinePlayer")
    offlinePlayer.addBroken(10)
    offlinePlayer.addPlaced(10)
    offlinePlayer.addPenaltyScore(200)
    offlinePlayer.addMinutesPlayed(60)
    offlinePlayer.calculateScore
    PlayerRepository.save(offlinePlayer)

    val session = new Session("OnlinePlayer", "127.0.0.1")
    session.blocksBroken = 10
    session.blocksPlaced = 10

    SessionMap.put("OnlinePlayer", session)

    onlinePlayer = new Player("OnlinePlayer")
    onlinePlayer.addBroken(10)
    onlinePlayer.addPlaced(10)
    onlinePlayer.addPenaltyScore(200)
    onlinePlayer.addMinutesPlayed(60)
    onlinePlayer.calculateScore
    PlayerRepository.save(onlinePlayer)
  }

  override def afterEach() {
    clearTestData()
    SessionMap.clear()
  }

  it should "return false if there are too many arguments" in {
    val admin = new TestPlayer("Admin")
    admin.setOp(true)
    val result = PlayerScoreExecutor.onCommand(admin, null, "score", Array("1", "TooMany"))

    result should equal (false)
  }

  it should "prevent non admins from looking at other people's scores" in {
    val nonAdmin = new TestPlayer("nonAdmin")
    val result = PlayerScoreExecutor.onCommand(nonAdmin, null, "score", Array("OnlinePlayer"))

    result should equal (true)
    nonAdmin.getMessage should equal ("You do not have access to that command!")
  }

  it should "not show a score if the player does not exist" in {
    val admin = new TestPlayer("Admin")
    admin.setOp(true)
    val result = PlayerScoreExecutor.onCommand(admin, null, "score", Array("NotAPlayer"))

    result should equal (true)
    admin.getMessage() should equal ("Player " + DARK_PURPLE + "NotAPlayer" + WHITE + " does not exist.  " +
      "Please double check the spelling.")
  }

  it should "show the score of an offline player" in {
    val now = new DateTime
    TimeFreezeService.freeze(now.plusDays(1))
    val admin = new TestPlayer("Admin")
    admin.setOp(true)
    val result = PlayerScoreExecutor.onCommand(admin, null, "score", Array("OfflinePlayer"))

    result should equal (true)
    admin.getMessage() should equal ("Player " + DARK_PURPLE + "OfflinePlayer" + WHITE +
      " has a score of " + DARK_AQUA + offlinePlayer.score + WHITE + ".")

    TimeFreezeService.unfreeze()
  }

  it should "show an updated score for an online player" in {
    val now = SessionMap.get("OnlinePlayer").get.connectedAt.plusMinutes(60)

    TimeFreezeService.freeze(now)
    val admin = new TestPlayer("Admin")
    admin.setOp(true)
    val result = PlayerScoreExecutor.onCommand(admin, null, "score", Array("OnlinePlayer"))

    result should equal (true)
    admin.getMessage() should equal ("Player " + DARK_PURPLE + "OnlinePlayer" + WHITE +
      " has a score of " + DARK_AQUA + 4600 + WHITE + ".")

    TimeFreezeService.unfreeze()
  }
}
