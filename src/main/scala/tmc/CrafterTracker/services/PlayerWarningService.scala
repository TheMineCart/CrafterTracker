package tmc.CrafterTracker.services

import actors.Actor
import tmc.CrafterTracker.CtPlugin
import org.bukkit.ChatColor

// Created by cyrus on 5/14/12 at 3:47 PM

object PlayerWarningService extends Actor {

  var active: Boolean = false
  var sleepPeriod: Int = 10000

  def act() {
    while(active) {
      Thread.sleep(sleepPeriod)
      CtPlugin.server.getOnlinePlayers.foreach(player => {
        WarningMessageRepository.findUnacknowledgedByPlayerName(player.getName).foreach(message => {
          player.sendMessage("")
          player.sendMessage("* You have been warned for \"" + ChatColor.RED  + message.text + ChatColor.WHITE + "\"")
          player.sendMessage("* This is a " + message.infraction.chatOutput + " infraction and you have lost " +
                             ChatColor.DARK_AQUA + message.score + ChatColor.WHITE + " points")
          player.sendMessage("* Please execute " + ChatColor.RED + "/acknowledge " +
                             (message.issuedAt.toString(CtPlugin.warningIdFormat)) +
                             ChatColor.WHITE + " to stop this message from repeating.")
        })
      })
    }
  }

}
