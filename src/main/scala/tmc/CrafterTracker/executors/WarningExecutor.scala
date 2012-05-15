package tmc.CrafterTracker.executors

import org.bukkit.command.{Command, CommandSender, CommandExecutor}
import tmc.CrafterTracker.services.{WarningMessageRepository, PlayerRepository}
import org.bukkit.ChatColor
import tmc.CrafterTracker.domain._
import tmc.CrafterTracker.CtPlugin


// Created by cyrus on 5/9/12 at 1:10 PM

object WarningExecutor extends CommandExecutor {

  def onCommand(sender: CommandSender, command: Command, commandName: String, args: Array[String]): Boolean = {
    if (!sender.isOp) {
      sender.sendMessage("You do not have access to that command!")
      return true
    }
    if (args.length < 3) {
      return false
    }
    if (!PlayerRepository.exists(args(0))) {
      sender.sendMessage("Could not find player " + ChatColor.DARK_PURPLE + args(0) + ChatColor.WHITE + ". Please double check your spelling.")
      return true
    }
    if (matchInfraction(args(1)) == None) {
      sender.sendMessage("No matching infraction for " + ChatColor.DARK_PURPLE + "" +
        args(1) + ChatColor.WHITE + ". Please double check your spelling.")
      return true
    }
    val iterator = CtPlugin.server.getOperators.iterator()
    while (iterator.hasNext) {
      val next = iterator.next()
      if (next.getName.equals(args(0))) {
        sender.sendMessage(ChatColor.DARK_RED + "You cannot warn another server operator!")
        return true
      }
    }
    val player = PlayerRepository.findByPlayerName(args(0))
    val message: String = args.slice(2, args.length).mkString(" ")
    val infraction = matchInfraction(args(1)).get
    val warning = new WarningMessage(sender.getName, args(0), message, infraction, player.score)

    player.addPenaltyScore(warning.score)
    player.calculateScore

    WarningMessageRepository.save(warning)
    PlayerRepository.save(player)

    sender.sendMessage("Successfully sent warning to " + ChatColor.DARK_PURPLE + args(0) + ChatColor.WHITE + ".")
    sendOnlineOpsNotification(sender.getName, args(0), infraction)
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

  private def sendOnlineOpsNotification(senderName: String, recipientName: String, infraction: Infraction) {
    CtPlugin.server.getOnlinePlayers.filter(p => p.isOp && p.getName != senderName)
      .map(p => p.sendMessage("Player " + ChatColor.DARK_PURPLE + recipientName + ChatColor.WHITE +
      " has received a " + infraction.toString + " warning."))
  }
}
