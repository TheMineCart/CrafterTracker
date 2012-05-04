package tmc.CrafterTracker

import domain.{Session, SessionMap}
import executors.SessionInformationExecutor
import listener.{PlayerInteractionListener, PlayerConnectionListener}
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.Server
import services.SessionRepository

// Created by cyrus on 5/1/12 at 10:18 AM

class CrafterTrackerPlugin extends JavaPlugin {

  var server: Server = null
  var sessionRepository: SessionRepository = null

  override def onEnable() {
    server = getServer
    initializeMongoDB()
    initializeDatabase()
    initializeRepositories()
    initializeCollectionIndexes()
    registerCommandExecutors()
    registerEventListeners()
    setupSessionMap()
  }

  override def onDisable() {
    tearDownSessionMap()
  }

  def setupSessionMap() {
    server.getOnlinePlayers.foreach(player => {
      SessionMap.put(player.getName, new Session(player.getName, player.getAddress.toString))
    })
  }

  def tearDownSessionMap(){
    server.getOnlinePlayers.foreach(player => {
      val session: Session = SessionMap.get(player.getName)
      session.disconnected
      sessionRepository.save(session)
    })
    SessionMap.clear()
  }

  def initializeMongoDB() {

  }

  def initializeDatabase() {

  }

  def initializeRepositories() {
    sessionRepository = new SessionRepository

  }

  def initializeCollectionIndexes() {

  }

  def registerCommandExecutors() {
    getCommand("sessioninfo").setExecutor(new SessionInformationExecutor)
  }

  def registerEventListeners() {
    server.getPluginManager().registerEvents(new PlayerConnectionListener(new SessionRepository), this)
    server.getPluginManager().registerEvents(new PlayerInteractionListener, this)
  }

}
