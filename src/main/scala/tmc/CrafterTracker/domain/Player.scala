package tmc.CrafterTracker.domain

import org.joda.time.{Days, DateTime}


// Created by cyrus on 5/8/12 at 4:48 PM

class Player(name: String) {
  var username: String = name

  var joinedOn: DateTime = new DateTime()
  var minutesPlayed : Long = 0
  var blocksPlaced: Int = 0
  var blocksBroken: Int = 0
  var penaltyScore : Int = 0
  var score : Long = 0

  def addMinutesPlayed(minutes: Long) {
    minutes >= 0 match {
      case true => minutesPlayed += minutes
      case false => throw new IllegalArgumentException("Minutes played must be greater than or equal to 0")
    }
  }

  def addBroken(numBlocksBroken: Int) {
    numBlocksBroken >= 0 match {
      case true => blocksBroken += numBlocksBroken
      case false => throw new IllegalArgumentException("Number of blocks broken must be greater than 0")
    }
  }

  def addPlaced(numBlocksPlaced: Int) {
    numBlocksPlaced >= 0 match {
      case true => blocksPlaced += numBlocksPlaced
      case false => throw new IllegalArgumentException("Number of blocks placed must be greater than 0")
    }
  }

  def addPenaltyScore(score: Int) {
    score >= 0 match {
      case true => penaltyScore += score
      case false => throw new IllegalArgumentException("Penalty score must be greater than or equal to 0")
    }
  }

  def calculateScore {
    println(Days.daysBetween(new DateTime(), joinedOn).getDays)
    score = ((minutesPlayed/(1 + Days.daysBetween(new DateTime, joinedOn).getDays)) * (blocksPlaced + blocksBroken)) - penaltyScore
  }
}
