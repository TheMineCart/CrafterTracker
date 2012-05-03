package tmc.CrafterTracker

import listener.PlayerConnectionListener
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.Server
import services.SessionRepository

// Created by cyrus on 5/1/12 at 10:18 AM

class CrafterTrackerPlugin extends JavaPlugin {

  var server: Server = null

  override def onEnable() {
    server = getServer
    server.getPluginManager().registerEvents(new PlayerConnectionListener(server, new SessionRepository), this)

    getLogger.info("***********************************************")
    getLogger.info("Hello World")
    getLogger.info("***********************************************")
  }

  override def onDisable() {
    getLogger.info("shutting down....")
  }

}
