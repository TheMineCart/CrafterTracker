package tmc.CrafterTracker.listener

import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.event.block.{BlockPlaceEvent, BlockBreakEvent}
import tmc.CrafterTracker.domain.{Session, SessionMap}
import tmc.CrafterTracker.services.WarningMessageRepository
import org.bukkit.ChatColor


// Created by cyrus on 5/3/12 at 11:54 AM

object PlayerInteractionListener extends Listener {

  @EventHandler
  def onBlockBreak(event: BlockBreakEvent) {
    if (hasNoUnacknowledgedWarningsFor(event.getPlayer.getName))
      SessionMap.applyToSessionFor(event.getPlayer.getName, (s: Session) => s.blockBroken)
    else {
      event.setCancelled(true)
      event.getPlayer.sendMessage(ChatColor.DARK_RED + "You cannot break any blocks until you acknowledge your warnings.")
    }
  }

  @EventHandler
  def onBlockPlace(event: BlockPlaceEvent) {
    if (hasNoUnacknowledgedWarningsFor(event.getPlayer.getName))
      SessionMap.applyToSessionFor(event.getPlayer.getName, (s: Session) => s.blockPlaced)
    else {
      event.setCancelled(true)
      event.getPlayer.sendMessage(ChatColor.DARK_RED + "You cannot place any blocks until you acknowledge your warnings.")
    }
  }

  private def hasNoUnacknowledgedWarningsFor(playerName: String): Boolean = {
    WarningMessageRepository.findUnacknowledgedByPlayerName(playerName).isEmpty
  }
}
