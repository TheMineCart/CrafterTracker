package tmc.CrafterTracker.listener

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import tmc.CrafterTracker.builders.aSession
import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.bukkit.event.block.{BlockPlaceEvent, BlockBreakEvent}
import tmc.BukkitTestUtilities.Mocks.{TestBlock, TestBlockState, TestPlayer}
import tmc.CrafterTracker.services.{WarningMessageRepository, PlayerRepository}
import tmc.CrafterTracker.domain.{Minor, WarningMessage, Player, SessionMap}
import org.bukkit.{ChatColor, Material}

// Created by cyrus on 5/3/12 at 11:55 AM

@RunWith(classOf[JUnitRunner])
class PlayerInteractionListenerTest extends FlatSpec with ShouldMatchers with BeforeAndAfterEach {
  val listener = PlayerInteractionListener

  override def afterEach() {
    SessionMap.clear()
  }

  it should "allow a player to break blocks if he has only acknowledged warnings" in {
    PlayerRepository.save(new Player("Sam"))
    val message = new WarningMessage("Jason", "Sam", "can't break blocks here", Minor, 3000)
    message.acknowledge
    WarningMessageRepository.save(message)

    val event = new BlockBreakEvent(new TestBlock(1, 2, 3), new TestPlayer("Sam"))

    listener.onBlockBreak(event)

    event.isCancelled should equal (false)
  }

  it should "allow a player to place block if he has only acknowledged warnings" in {
    PlayerRepository.save(new Player("Sam"))
    val message = new WarningMessage("Jason", "Sam", "can't place blocks here", Minor, 3000)
    message.acknowledge
    WarningMessageRepository.save(message)

    val event = new BlockPlaceEvent(new TestBlock(1, 2, 3), new TestBlockState(Material.AIR),
      null, null, new TestPlayer("Sam"), true)

    listener.onBlockPlace(event)

    event.isCancelled should equal (false)
  }

  it should "prevent a player from breaking any blocks if he has any unacknowledged warning" in {
    PlayerRepository.save(new Player("Sam"))
    WarningMessageRepository.save(new WarningMessage("Jason", "Sam", "can't break blocks here", Minor, 3000))

    val sam = new TestPlayer("Sam")
    val event = new BlockBreakEvent(new TestBlock(1, 2, 3), sam)

    listener.onBlockBreak(event)

    sam.getMessage should equal (ChatColor.DARK_RED + "You cannot break any blocks until you acknowledge your warnings.")

    event.isCancelled should equal (true)
  }

  it should "prevent a player from placing any blocks if he has any unacknowledged warning" in {
    PlayerRepository.save(new Player("Sam"))
    WarningMessageRepository.save(new WarningMessage("Jason", "Sam", "can't place blocks here", Minor, 3000))

    val sam = new TestPlayer("Sam")
    val event = new BlockPlaceEvent(new TestBlock(1, 2, 3), new TestBlockState(Material.AIR),
      null, null, sam, true)

    listener.onBlockPlace(event)

    sam.getMessage should equal (ChatColor.DARK_RED + "You cannot place any blocks until you acknowledge your warnings.")

    event.isCancelled should equal (true)
  }

  it should "increment blocksBroken when a BlockBreakEvent is fired" in {
    var session = aSession.build
    SessionMap.put("Jason", session)
    val event = new BlockBreakEvent(new TestBlock(1, 2, 3), new TestPlayer("Jason"))

    SessionMap.get("Jason").get.blocksBroken should equal(0)
    listener.onBlockBreak(event)
    SessionMap.get("Jason").get.blocksBroken should equal(1)
    event.isCancelled should equal (false)
  }

  it should "not expload when a BlockBreakEvent is fired by a player who doesn't have a session" in {
    val event = new BlockBreakEvent(new TestBlock(1, 2, 3), new TestPlayer("Sam"))

    listener.onBlockBreak(event)
    SessionMap.sessions.size should equal (0)
  }

  it should "increment blocksPlaced when a BlockPlaceEvent is fired" in {
    var session = aSession.build
    SessionMap.put("Jason", session)
    val event = new BlockPlaceEvent(new TestBlock(1, 2, 3), new TestBlockState(Material.AIR),
      null, null, new TestPlayer("Jason"), true)

    SessionMap.get("Jason").get.blocksPlaced should equal(0)
    listener.onBlockPlace(event)
    SessionMap.get("Jason").get.blocksPlaced should equal(1)
    event.isCancelled should equal (false)
  }

  it should "not expload when a BlockPlaceEvent is fired by a player who doesn't have a session" in {
    val event = new BlockPlaceEvent(new TestBlock(1, 2, 3), new TestBlockState(Material.AIR),
      null, null, new TestPlayer("Sam"), true)


    listener.onBlockPlace(event)
    SessionMap.sessions.size should equal (0)
  }
}
