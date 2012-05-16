package tmc.CrafterTracker

import org.bukkit.Server
import java.util.logging.Logger
import org.bukkit.plugin.java.JavaPlugin

// Created by cyrus on 5/10/12 at 10:32 AM

object CtPlugin {

  var plugin: JavaPlugin = null
  var server: Server = null
  var logger: Logger = null
  val warningIdFormat = "yyMMddHHmm"

  def initialize(p: JavaPlugin) {
    this.plugin = p
    server = plugin.getServer
    logger = plugin.getLogger
  }
}
