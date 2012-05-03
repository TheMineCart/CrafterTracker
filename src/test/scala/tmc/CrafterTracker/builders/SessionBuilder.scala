package tmc.CrafterTracker.builders

import tmc.CrafterTracker.domain.Session

// Created by cyrus on 5/3/12 at 10:47 AM

object aSession extends SessionBuilder {

}

class SessionBuilder {

  var ipAddress = "127.0.0.1"
  var playerName = "Jason"

  def withPlayerName(playerName: String): SessionBuilder = {
    this.playerName = playerName
    this
  }

  def withIpAddress(ipAddress: String): SessionBuilder = {
    this.ipAddress = ipAddress
    this
  }

  def build(): Session = {
    new Session(playerName, ipAddress)
  }
}
