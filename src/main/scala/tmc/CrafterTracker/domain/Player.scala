package tmc.CrafterTracker.domain

import org.joda.time.{Days, DateTime}
import com.google.gson.annotations.Expose


// Created by cyrus on 5/8/12 at 4:48 PM

class Player(name: String) {
  @Expose var username: String = name
  @Expose var joinedOn: DateTime = new DateTime()
  @Expose var minutesPlayed : Long = 0
  @Expose var blocksPlaced: Int = 0
  @Expose var blocksBroken: Int = 0
  @Expose var penaltyScore : Long = 0
  @Expose var score : Long = 0

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

  def addPenaltyScore(score: Long) {
    score >= 0 match {
      case true => penaltyScore += score
      case false => throw new IllegalArgumentException("Penalty score must be greater than or equal to 0")
    }
  }

  def averageMinutesPlayed = {
    minutesPlayed/(1 + Days.daysBetween(joinedOn, new DateTime).getDays)
  }

  def calculateScore {
    score = (averageMinutesPlayedValue * (blocksPlacedValue + blocksBrokenValue)) - penaltyScore
    if (score < 0) score = 0
  }

  private def averageMinutesPlayedValue: Int = {
    (averageMinutesPlayed * Coefficient.averageMinutesMultiplier).toInt
  }

  private def blocksPlacedValue: Int = {
    (blocksPlaced * Coefficient.blocksPlacedMultiplier).toInt
  }

  private def blocksBrokenValue: Int = {
    (blocksBroken * Coefficient.blocksBrokenMultiplier).toInt
  }
}
