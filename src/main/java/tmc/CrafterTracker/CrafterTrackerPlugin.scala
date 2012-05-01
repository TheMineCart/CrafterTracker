package tmc.CrafterTracker

import org.bukkit.plugin.java.JavaPlugin

// Created by cyrus on 5/1/12 at 10:18 AM

class CrafterTrackerPlugin extends JavaPlugin {

  override def onEnable() {
    getLogger.info("***********************************************")
    getLogger.info("Hello World")
    getLogger.info("***********************************************")
  }

  override def onDisable() {
    getLogger.info("shutting down....")
  }

}
