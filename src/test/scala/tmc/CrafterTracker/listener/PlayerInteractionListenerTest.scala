package tmc.CrafterTracker.listener

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import tmc.CrafterTracker.builders.aSession
import org.bukkit.event.block.BlockBreakEvent
import tmc.BukkitTestUtilities.Mocks.{TestPlayer, TestBlock}
import tmc.CrafterTracker.domain.SessionMap

// Created by cyrus on 5/3/12 at 11:55 AM

@RunWith(classOf[JUnitRunner])
class PlayerInteractionListenerTest extends FlatSpec with ShouldMatchers {
  val listener: PlayerInteractionListener = new PlayerInteractionListener()

  "The listener" should "add a session to the sessions map when a player joins" in {
    var session = aSession.build
    val event = new BlockBreakEvent(new TestBlock(1, 2, 3), new TestPlayer("Jason"))

    listener.onBlockBreak(event)

    SessionMap.sessions

  }

}
