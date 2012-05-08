package tmc.CrafterTracker.listener

import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.event.block.{BlockPlaceEvent, BlockBreakEvent}
import tmc.CrafterTracker.domain.SessionMap


// Created by cyrus on 5/3/12 at 11:54 AM

class PlayerInteractionListener extends Listener {

  @EventHandler
  def onBlockBreak(event: BlockBreakEvent) = {
    val playerName = event.getPlayer.getName
    SessionMap.get(playerName).map((session) => {
      session.blockBroken
    })
  }

  @EventHandler
  def onBlockPlace(event: BlockPlaceEvent) {
    val playerName = event.getPlayer.getName
    SessionMap.get(playerName).map((session) => {
      session.blockPlaced
    })
  }
}
