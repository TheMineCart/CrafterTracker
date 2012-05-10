package tmc.CrafterTracker.executors

import org.bukkit.command.{Command, CommandSender, CommandExecutor}
import tmc.CrafterTracker.services.{WarningMessageRepository, PlayerRepository}
import org.bukkit.ChatColor
import tmc.CrafterTracker.domain._


// Created by cyrus on 5/9/12 at 1:10 PM

class WarningExecutor extends CommandExecutor {
  override def onCommand(commandSender: CommandSender, command: Command, commandName: String, args: Array[String]): Boolean = {
    if (!commandSender.isOp) {
      commandSender.sendMessage("You do not have access to that command!")
      return true
    }
    if (args.length < 3) {
      return false
    }
    if (!PlayerRepository.exists(args(0))) {
      commandSender.sendMessage("Could not find player " + ChatColor.DARK_PURPLE + args(0) + ChatColor.WHITE + ". Please double check your spelling.")
      return true
    }
    if (matchInfraction(args(1)) == None) {
      commandSender.sendMessage("No matching infraction for " + ChatColor.DARK_PURPLE + "" +
                                args(1) + ChatColor.WHITE + ". Please double check your spelling.")
      return true
    }

    val player = PlayerRepository.findByPlayerName(args(0))
    val message: String = args.slice(2, args.length).mkString(" ")
    val warning = new WarningMessage(commandSender.getName, args(0), message, matchInfraction(args(1)).get, player.score)

    player.addPenaltyScore(warning.score)
    player.calculateScore

    WarningMessageRepository.save(warning)
    PlayerRepository.save(player)

    commandSender.sendMessage("Successfully sent warning to " + ChatColor.DARK_PURPLE + args(0) + ChatColor.WHITE + ".")

    true
  }

  private def matchInfraction(infraction: String): Option[Infraction] = {
    infraction.toUpperCase match {
      case "MINOR" => Some(Minor)
      case "MODERATE" => Some(Moderate)
      case "MAJOR" => Some(Major)
      case _ => None
    }
  }
}
