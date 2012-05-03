package tmc.CrafterTracker.executors

import org.bukkit.command.{Command, CommandSender, CommandExecutor}
import tmc.CrafterTracker.domain.{Session, SessionMap}


// Created by cyrus on 5/3/12 at 3:28 PM

class SessionInformationExecutor extends CommandExecutor{

  override def onCommand(p1: CommandSender, p2: Command, p3: String, p4: Array[String]): Boolean = {
    val session : Session = SessionMap.get(p1.getName)
    val message : String = "Blocks broken: " + session.blocksBroken + ", " +
                           "Blocks placed: " + session.blocksPlaced + ", " +
                           "Connected at: " + session.connectedAt.toString("MM/dd/yyyy hh:mm:ss") + "," +
                           "IP address: " + session.ipAddress

    p1.getServer.getPlayer(p1.getName).sendMessage(message)
    true
  }
}
