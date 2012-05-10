package tmc.CrafterTracker.listener

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import tmc.CrafterTracker.builders.aSession
import tmc.CrafterTracker.domain.SessionMap
import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.bukkit.event.block.{BlockPlaceEvent, BlockBreakEvent}
import org.bukkit.Material
import tmc.BukkitTestUtilities.Mocks.{TestBlock, TestBlockState, TestPlayer}

// Created by cyrus on 5/3/12 at 11:55 AM

@RunWith(classOf[JUnitRunner])
class PlayerInteractionListenerTest extends FlatSpec with ShouldMatchers with BeforeAndAfterEach {
  val listener = PlayerInteractionListener

  override def afterEach() {
    SessionMap.clear()
  }

  "The listener" should "increment blocksBroken when a BlockBreakEvent is fired" in {
    var session = aSession.build
    SessionMap.put("Jason", session)
    val event = new BlockBreakEvent(new TestBlock(1, 2, 3), new TestPlayer("Jason"))

    SessionMap.get("Jason").get.blocksBroken should equal(0)
    listener.onBlockBreak(event)
    SessionMap.get("Jason").get.blocksBroken should equal(1)
  }

  "The listener" should "not expload when a BlockBreakEvent is fired by a player who doesn't have a session" in {
    val event = new BlockBreakEvent(new TestBlock(1, 2, 3), new TestPlayer("Sam"))

    listener.onBlockBreak(event)
    SessionMap.sessions.size should equal (0)
  }

  "The listener" should "increment blocksPlaced when a BlockPlaceEvent is fired" in {
    var session = aSession.build
    SessionMap.put("Jason", session)
    val event = new BlockPlaceEvent(new TestBlock(1, 2, 3), new TestBlockState(Material.AIR),
      null, null, new TestPlayer("Jason"), true)

    SessionMap.get("Jason").get.blocksPlaced should equal(0)
    listener.onBlockPlace(event)
    SessionMap.get("Jason").get.blocksPlaced should equal(1)
  }

  "The listener" should "not expload when a BlockPlaceEvent is fired by a player who doesn't have a session" in {
    val event = new BlockPlaceEvent(new TestBlock(1, 2, 3), new TestBlockState(Material.AIR),
      null, null, new TestPlayer("Sam"), true)


    listener.onBlockPlace(event)
    SessionMap.sessions.size should equal (0)
  }
}
