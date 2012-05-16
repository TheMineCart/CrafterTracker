package tmc.CrafterTracker

import domain.{Moderate, Minor, Major}
import org.bukkit.configuration.file.FileConfiguration
import java.util.TreeMap
import services.PlayerWarningService

// Created by cyrus on 5/16/12 at 10:03 AM

object Configuration {

  private val DB_ADDRESS_OPTION: String = "address"
  private val DB_NAME_OPTION: String = "dbName"
  private val DB_USER_OPTION: String = "dbUser"
  private val DB_PASSWORD_OPTION: String = "dbPassword"

  private val MINOR_OPTION: String = "minor"
  private val MODERATE_OPTION: String = "moderate"
  private val MAJOR_OPTION: String = "major"

  private val BLOCK_EVENT_OPTION: String = "PluginOptions.enableBlockEventCancel"
  private val FREQUENCY_OPTION: String = "PluginOptions.frequencyOfWarningsInChat"

  private var configuration: FileConfiguration = null
  private val MONGODB_SECTION: String = "MongodDbConnectionInfo"
  private val INFRACTION_SECTION: String = "InfractionMultipliers"

  private val dbConnectionInfo: TreeMap[String, String] =  new TreeMap[String, String]
  private val infractionMultipliers: TreeMap[String, Float] =  new TreeMap[String, Float]

  private var enableBlockEventCancel: Boolean = true
  private var frequencyOfWarningDisplay: Int = 10

  def dbName: String = dbConnectionInfo.get(DB_NAME_OPTION)
  def dbAddress: String = dbConnectionInfo.get(DB_ADDRESS_OPTION)
  def dbUser: String = dbConnectionInfo.get(DB_USER_OPTION)
  def dbPassword: String = dbConnectionInfo.get(DB_PASSWORD_OPTION)
  def minorInfraction: Float = infractionMultipliers.get(MINOR_OPTION)
  def moderateInfraction: Float = infractionMultipliers.get(MODERATE_OPTION)
  def majorInfraction: Float = infractionMultipliers.get(MAJOR_OPTION)
  def canCancelBlockEvents: Boolean = this.enableBlockEventCancel
  def warningOutputFrequency: Int = frequencyOfWarningDisplay

  def initialize = {
    configuration = CtPlugin.plugin.getConfig()
    if(configuration.getKeys(false).isEmpty) {
      registerDefaultValues;
    } else {
      registerConfigurationValues
    }

    Minor.penalty = Configuration.minorInfraction
    Moderate.penalty = Configuration.moderateInfraction
    Major.penalty = Configuration.majorInfraction
    PlayerWarningService.sleepPeriod = Configuration.warningOutputFrequency * 1000
  }

  private def registerDefaultValues {
    configuration.options().header("CrafterTracker Configuration\n" +
      " - Delete the dbUser and dbPassword elements if you are not using any authentication for your MongoDB. \n" +
      " - The InfractionMultipliers for Minor, Moderate, and Major infractions must be between 0 and 1. \n" +
      " - enableBlockEventCancel must be a boolean (true or false) which controls whether or not a player \n\tcan place blocks when they have unacknowledged messages.\n" +
      " - frequencyOfWarningsInChat must be an integer that represents the time in seconds between warning reminders.")

    dbConnectionInfo.put(DB_NAME_OPTION, "CrafterTracker")
    dbConnectionInfo.put(DB_ADDRESS_OPTION, "127.0.0.1")
    dbConnectionInfo.put(DB_USER_OPTION, "user")
    dbConnectionInfo.put(DB_PASSWORD_OPTION, "password")
    configuration.createSection(MONGODB_SECTION, dbConnectionInfo)


    infractionMultipliers.put(MINOR_OPTION, 0.1F)
    infractionMultipliers.put(MODERATE_OPTION, 0.25F)
    infractionMultipliers.put(MAJOR_OPTION, 0.5F)
    configuration.createSection(INFRACTION_SECTION, infractionMultipliers)

    configuration.set(BLOCK_EVENT_OPTION, true)
    configuration.set(FREQUENCY_OPTION, 10)

    CtPlugin.plugin.saveConfig()
  }

  private def registerConfigurationValues {
    var section = configuration.getConfigurationSection(MONGODB_SECTION)
    dbConnectionInfo.put(DB_NAME_OPTION, section.getString(DB_NAME_OPTION))
    dbConnectionInfo.put(DB_ADDRESS_OPTION, section.getString(DB_ADDRESS_OPTION))
    dbConnectionInfo.put(DB_USER_OPTION, section.getString(DB_USER_OPTION))
    dbConnectionInfo.put(DB_PASSWORD_OPTION, section.getString(DB_PASSWORD_OPTION))

    section = configuration.getConfigurationSection(INFRACTION_SECTION)
    infractionMultipliers.put(MINOR_OPTION, section.getDouble(MINOR_OPTION).toFloat)
    infractionMultipliers.put(MODERATE_OPTION, section.getDouble(MODERATE_OPTION).toFloat)
    infractionMultipliers.put(MAJOR_OPTION, section.getDouble(MAJOR_OPTION).toFloat)

    enableBlockEventCancel = configuration.getBoolean(BLOCK_EVENT_OPTION)
    frequencyOfWarningDisplay = configuration.getInt(FREQUENCY_OPTION)
  }
}
