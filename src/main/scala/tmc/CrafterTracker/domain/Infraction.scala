package tmc.CrafterTracker.domain

import org.bukkit.ChatColor


// Created by cyrus on 5/8/12 at 1:39 PM

trait Infraction {
  var penalty: Float

  def chatOutput: String

  def chatColor: ChatColor
}

object Minor extends Infraction {
  var penalty = 0.1F

  override def toString = {"minor"}

  def chatOutput = {ChatColor.YELLOW + "Minor" + ChatColor.WHITE}

  def chatColor = {ChatColor.YELLOW}
}

object Moderate extends Infraction {
  var penalty = 0.25F

  override def toString = {"moderate"}

  def chatOutput = {ChatColor.GOLD + "Moderate" + ChatColor.WHITE}

  def chatColor = {ChatColor.GOLD}
}

object Major extends Infraction {
  var penalty = 0.5F

  override def toString = {"major"}

  def chatOutput = {ChatColor.DARK_RED + "Major" + ChatColor.WHITE}

  def chatColor = {ChatColor.RED}
}
