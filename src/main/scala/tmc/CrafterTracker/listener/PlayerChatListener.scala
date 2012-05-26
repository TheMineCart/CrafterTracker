package tmc.CrafterTracker.listener

import org.bukkit.event.player.PlayerChatEvent
import tmc.CrafterTracker.services.WarningMessageRepository
import org.bukkit.ChatColor
import org.bukkit.event.{EventPriority, Listener, EventHandler}

object PlayerChatListener extends Listener {

  @EventHandler(priority = EventPriority.HIGH)
  def mutePlayersWithWarnings(event: PlayerChatEvent){
    if (WarningMessageRepository.findUnacknowledgedByPlayerName(event.getPlayer.getName).size > 0) {
      event.setCancelled(true)
      event.getPlayer.sendMessage(ChatColor.DARK_RED + "Sorry, but you are muted until you acknowledge your warnings!")
    }
  }
}
