package tmc.CrafterTracker.listener

import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.event.block.{BlockPlaceEvent, BlockBreakEvent}
import tmc.CrafterTracker.domain.{Session, SessionMap}


// Created by cyrus on 5/3/12 at 11:54 AM

object PlayerInteractionListener extends Listener {

  @EventHandler
  def onBlockBreak(event: BlockBreakEvent) =
    SessionMap.applyToSessionFor(event.getPlayer.getName, (s: Session) => s.blockBroken)

  @EventHandler
  def onBlockPlace(event: BlockPlaceEvent) =
    SessionMap.applyToSessionFor(event.getPlayer.getName, (s: Session) => s.blockPlaced)
}
