// Copyright (C) 2012 Cyrus Innovation
package tmc.CrafterTracker.executors

import org.bukkit.command.{Command, CommandSender, CommandExecutor}
import org.bukkit.ChatColor.{WHITE, DARK_AQUA, DARK_PURPLE, RED, DARK_RED, GRAY}
import tmc.CrafterTracker.services.{WarningMessageRepository, PlayerRepository}
import tmc.CrafterTracker.domain.WarningMessage


// Created by cyrus on 5/14/12 at 11:25 AM

object WarningInformationExecutor extends CommandExecutor {

  def onCommand(sender: CommandSender, command: Command, commandName: String, args: Array[String]): Boolean = {
    if (!sender.isOp) {
      sender.sendMessage("You do not have access to that command!")
      return true
    }
    if (args.length == 0 || args.length > 2) return false;
    if (!PlayerRepository.exists(args(0))) {
      sender.sendMessage("Player " + DARK_PURPLE + args(0) + WHITE +
        " does not exist. Please check your spelling.")
      return true
    }
    findAndDisplayWarningMessages(sender, args)
    true
  }

   private def findAndDisplayWarningMessages(sender: CommandSender, args: Array[String]) {
     val messages = WarningMessageRepository.findByPlayerName(args(0))

     var currentPage: List[WarningMessage] = null
     var pageMin = 1
     var pageMax = 5
     var nextPage = 0
     val totalPages = (messages.size / 5) + 1

     if (args.length == 1) {
       currentPage = messages.take(pageMax)
       nextPage = 2
     } else {
       pageMin = (args(1).toInt - 1) * 5
       pageMax = pageMin + 5
       currentPage = messages.slice(pageMin, pageMax)
       nextPage = args(1).toInt + 1
     }

     sendWarningMessageInfoToCommandSender(sender, args(0), currentPage, messages.size, pageMax, nextPage, totalPages)
   }

  private def sendWarningMessageInfoToCommandSender(sender: CommandSender, playerName: String, currentPage: List[WarningMessage], totalSize: Int, maxPageIndex: Int, nextPage: Int, totalPages: Int) {

    if (nextPage == 2) {
      sender.sendMessage("Player " + DARK_PURPLE + playerName + WHITE + " has received " +
        DARK_PURPLE + totalSize + WHITE + " warning messages." )
    } else if (nextPage - 1 > totalPages) {
      sender.sendMessage(DARK_RED + "Sorry but there is no page " + (nextPage - 1))
    } else {
      sender.sendMessage("Page " + RED + (nextPage - 1) + WHITE + " of " + RED + totalPages)
    }

    currentPage.foreach(
      message => sender.sendMessage(
        message.infraction.chatOutput + " infraction with a penalty of " +
          DARK_AQUA + message.score + WHITE + " points for \"" + GRAY + message.text + WHITE + "\""
      )
    )

    if (totalSize >= maxPageIndex) {
      sender.sendMessage("See page "+ RED + nextPage + WHITE + " by executing " +
                         RED + "/wf " + playerName + " " + nextPage)
    }
  }
}
