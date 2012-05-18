package tmc.CrafterTracker.executors

import org.bukkit.command.{Command, CommandSender, CommandExecutor}
import tmc.CrafterTracker.services.PlayerRepository
import org.bukkit.ChatColor.{DARK_PURPLE, DARK_AQUA, WHITE}
import org.joda.time.{DateTime, Minutes}
import tmc.CrafterTracker.domain.SessionMap


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
      SessionMap.get(playerName).map(
        (session) => {
          player.addBroken(session.blocksBroken)
          player.addPlaced(session.blocksPlaced)
          player.addMinutesPlayed(Minutes.minutesBetween(session.connectedAt, new DateTime()).getMinutes)
          player.calculateScore
        }
      )
      sender.sendMessage("Player " + DARK_PURPLE + playerName + WHITE +
                         " has a score of " + DARK_AQUA + player.score + WHITE + ".")

    } else {
      sender.sendMessage("Player " + DARK_PURPLE + playerName + WHITE + " does not exist.  " +
                         "Please double check the spelling.")
    }
    true
  }
}
