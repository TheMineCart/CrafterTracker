package tmc.CrafterTracker.executors

import org.bukkit.command.{Command, CommandSender, CommandExecutor}
import tmc.CrafterTracker.services.PlayerRepository
import org.bukkit.ChatColor


// Created by cyrus on 5/11/12 at 5:21 PM

object PlayerScoreExecutor extends CommandExecutor {

  def onCommand(sender: CommandSender, command: Command, commandName: String, args: Array[String]): Boolean = {
    if (!sender.isOp && args.length > 0) {
      sender.sendMessage("You do not have access to that command!")
      return true
    }

    if (args.length > 1) return false

    var playerName = sender.getName
    if (args.length == 1) {
      playerName = args(0)

    }
    if (PlayerRepository.exists(playerName)) {
      val player = PlayerRepository.findByPlayerName(playerName)
      sender.sendMessage("Player " + ChatColor.DARK_PURPLE + playerName + ChatColor.WHITE +
                          " has a score of " + ChatColor.DARK_AQUA + player.score + ChatColor.WHITE + ".")

    } else {
      sender.sendMessage("Player " + ChatColor.DARK_PURPLE + playerName + ChatColor.WHITE + " does not exist.  " +
                         "Please double check the spelling.")
    }
    true
  }
}
