package tmc.CrafterTracker.executors

import org.bukkit.command.{Command, CommandSender, CommandExecutor}
import tmc.CrafterTracker.domain.{Session, SessionMap}
import org.bukkit.entity.Player


// Created by cyrus on 5/3/12 at 3:28 PM

class SessionInformationExecutor extends CommandExecutor {

  override def onCommand(sender: CommandSender, command: Command, s: String, args: Array[String]): Boolean = {
    val player: Player = sender.getServer.getPlayer(sender.getName)
    SessionMap.applyToSessionFor(player.getName, (s:Session) => sendMessageTo(player, s))
    true
  }

  private def sendMessageTo(player: Player, s: Session) = {
    player.sendMessage("Blocks broken: " + s.blocksBroken + ", " +
      "Blocks placed: " + s.blocksPlaced + ", " +
      "Connected at: " + s.connectedAt.toString("MM/dd/yyyy hh:mm:ss") + "," +
      "IP address: " + s.ipAddress)
  }
}
