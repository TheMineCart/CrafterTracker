// Copyright (C) 2012 Cyrus Innovation
package tmc.CrafterTracker.executors

import org.bukkit.command.{Command, CommandSender, CommandExecutor}
import org.bukkit.ChatColor
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import tmc.CrafterTracker.services.WarningMessageRepository
import tmc.CrafterTracker.CtPlugin


// Created by cyrus on 5/15/12 at 9:42 AM

object AcknowledgementExecutor extends CommandExecutor {

  def onCommand(sender: CommandSender, command: Command, commandName: String, args: Array[String]): Boolean = {
    if (args.length != 1) return false
    var warningIssuedAt: DateTime = null
    try {
      warningIssuedAt = DateTime.parse(args(0), DateTimeFormat.forPattern(CtPlugin.warningIdFormat))
    } catch {
      case e: IllegalArgumentException => {
        sender.sendMessage(ChatColor.DARK_RED + "Incorrect parameter, must be a number matching a warning that you have receieved.")
        return true;
      }
    }

    if(WarningMessageRepository.acknowledgeWarningFor(sender.getName, warningIssuedAt)) {
      sender.sendMessage(ChatColor.DARK_GREEN + "You have successfully acknowledged warning #" + args(0))
    } else {
      sender.sendMessage(ChatColor.DARK_RED + "Incorrect parameter, no warnings match #" + args(0))
    }

    true
  }
}
