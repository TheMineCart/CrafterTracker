package tmc.CrafterTracker.executors

import org.bukkit.command.{Command, CommandSender, CommandExecutor}
import org.bukkit.entity.Player
import tmc.CrafterTracker.domain.{Session, SessionMap}
import org.bukkit.ChatColor

// Created by cyrus on 5/3/12 at 3:28 PM

object SessionInformationExecutor extends CommandExecutor {

  def onCommand(sender: CommandSender, command: Command, s: String, args: Array[String]): Boolean = {
    val player: Player = sender.getServer.getPlayer(sender.getName)
    SessionMap.applyToSessionFor(player.getName, (s: Session) => sendMessageTo(sender, s))
    true
  }

  private def sendMessageTo(sender: CommandSender, s: Session) {
    sender.sendMessage("You broke " + ChatColor.DARK_PURPLE + s.blocksBroken + ChatColor.WHITE + " blocks and placed " +
                       ChatColor.DARK_PURPLE + s.blocksPlaced + ChatColor.WHITE + " blocks since you connected from " +
                       ChatColor.DARK_PURPLE + s.ipAddress  + ChatColor.WHITE +  " at " +
                       ChatColor.DARK_PURPLE + s.connectedAt.toString("MM/dd/yyyy hh:mm:ss") + ChatColor.WHITE + "." )
  }
}
