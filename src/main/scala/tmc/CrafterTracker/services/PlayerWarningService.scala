package tmc.CrafterTracker.services

import actors.Actor
import tmc.CrafterTracker.CtPlugin
import org.bukkit.ChatColor

// Created by cyrus on 5/14/12 at 3:47 PM

object PlayerWarningService extends Actor {

  var active: Boolean = false
  var sleepPeriod: Int = 30000

  def act() {
    while(active) {
      Thread.sleep(sleepPeriod)
      CtPlugin.server.getOnlinePlayers.foreach(player => {
        WarningMessageRepository.findUnacknowledgedByPlayerName(player.getName).zipWithIndex foreach(messageTuple => {
          player.sendMessage("You have been warned for " + ChatColor.RED +"\"" + messageTuple._1.text + "\"")
          player.sendMessage("This is a " + messageTuple._1.infraction.chatOutput + " infraction and you have lost " +
                             ChatColor.DARK_PURPLE + messageTuple._1.score + ChatColor.WHITE + " points")
          player.sendMessage("In order to stop this message from repeating, please execute /acknowledgewarning " + (messageTuple._2 + 1))
        })
      })
    }
  }

}
