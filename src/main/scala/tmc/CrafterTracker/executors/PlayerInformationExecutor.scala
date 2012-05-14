package tmc.CrafterTracker.executors

import org.bukkit.command.{Command, CommandSender, CommandExecutor}
import tmc.CrafterTracker.services.PlayerRepository
import tmc.CrafterTracker.domain.Player
import org.bukkit.ChatColor


// Created by cyrus on 5/14/12 at 9:57 AM

object PlayerInformationExecutor extends CommandExecutor {
  def onCommand(sender: CommandSender, command: Command, commandName: String, args: Array[String]): Boolean = {
    if(args.length > 1) return false
    else if (args.length == 1 && !sender.isOp) sender.sendMessage("You do not have access to that command!")
    else if (args.length == 0) {
      val player : Player = PlayerRepository.findByPlayerName(sender.getName)
      sendPlayerInfo(sender, player)
    } else if (!PlayerRepository.exists(args(0))) {
      sender.sendMessage("Player " + ChatColor.DARK_PURPLE + args(0) + ChatColor.WHITE + " does not exist. Please check your spelling.")
    } else {
      sendPlayerInfo(sender, PlayerRepository.findByPlayerName(args(0)))
    }
    true
  }

  def sendPlayerInfo(sender: CommandSender, player: Player) {
    sender.sendMessage("Player " + ChatColor.DARK_PURPLE + player.username + ChatColor.WHITE +
      " joined the server on " + ChatColor.DARK_PURPLE + player.joinedOn.toString("M/d/yyyy") + ChatColor.WHITE +
      " and has played an average of " + ChatColor.DARK_PURPLE + player.averageMinutesPlayed + ChatColor.WHITE + " minutes per day.")
  }
}
