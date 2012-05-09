package tmc.CrafterTracker.executors

import org.bukkit.command.{Command, CommandSender, CommandExecutor}
import tmc.CrafterTracker.services.{WarningMessageRepository, PlayerRepository}
import org.bukkit.{ChatColor, Server}
import tmc.CrafterTracker.domain._


// Created by cyrus on 5/9/12 at 1:10 PM

class WarningExecutor(s: Server, pR: PlayerRepository, wMR: WarningMessageRepository) extends CommandExecutor{
  val server: Server = s
  val playerRepository: PlayerRepository = pR
  val warningMessageRepository: WarningMessageRepository = wMR

  override def onCommand(commandSender: CommandSender, command: Command, commandName: String, args: Array[String]): Boolean = {
    if (!commandSender.isOp) {
      commandSender.sendMessage("You do not have access to that command!")
      return true;
    }
    if (args.length != 3) {
      return false;
    }
    if (!playerRepository.exists(args(0))) {
      commandSender.sendMessage("Could not find player " + ChatColor.DARK_PURPLE + args(0) + ChatColor.WHITE + ". Please double check your spelling.")
      return true;
    }

    val player = playerRepository.findByPlayerName(args(0))
    val warning = buildWarningMessage(commandSender.getName, args, player.score)

    player.addPenaltyScore(warning.score)
    player.calculateScore

    warningMessageRepository.save(warning)
    playerRepository.save(player)

    true
  }

  private def buildWarningMessage(sender: String, args: Array[String], playerScore: Long): WarningMessage = {
    var infraction: Infraction = null
    args(1).toUpperCase match {
      case "MINOR" => infraction = Minor
      case "MODERATE" => infraction = Moderate
      case "MAJOR" => infraction = Major
    }
    new WarningMessage(sender, args(0), args(2), infraction, playerScore)
  }
}
