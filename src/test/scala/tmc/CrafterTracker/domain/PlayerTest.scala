package tmc.CrafterTracker.domain

import tmc.BukkitTestUtilities.Services.TimeFreezeService
import org.joda.time.DateTime
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith

// Created by cyrus on 5/8/12 at 4:43 PM

@RunWith(classOf[JUnitRunner])
class PlayerTest extends FlatSpec with ShouldMatchers with BeforeAndAfterEach {
  var player : Player = null

  override def beforeEach {
    TimeFreezeService.freeze()
    player = new Player("Sam")
  }

  override def afterEach {
    TimeFreezeService.unfreeze()
  }

  it should "have a joined on date that is the date they joined the server" in {
    player.joinedOn should equal(new DateTime())
    player.username should equal("Sam")
  }

  it should "be able to increase the number of blocks broken" in {
    player.blocksBroken should equal(0)
    player.addBroken(21)
    player.blocksBroken should equal(21)
  }

  it should "not be able to increase the number of blocks broken by a negative number" in {
    intercept[IllegalArgumentException] {
     player.addBroken(-21)
    }
  }

  it should "be able to increase the number of blocks placed" in {
     player.blocksPlaced should equal(0)
     player.addPlaced(21)
     player.blocksPlaced should equal(21)
  }

  it should "not be able to increase the number of blocks placed by a negative number" in {
    intercept[IllegalArgumentException] {
      player.addPlaced(-21)
    }
  }

  it should "have an initial total duration of 0 minutes played" in {
    player.minutesPlayed should equal(0)
  }

  it should "be able to increase the number of minutes played" in {
    player.addMinutesPlayed(60)
    player.minutesPlayed should equal(60)
  }

  it should "not be able to increase the number of minutes played by a negative value" in {
    intercept[IllegalArgumentException] {
      player.addMinutesPlayed(-60)
    }
  }

  it should "initially have no penalties" in {
    player.penaltyScore should equal(0)
  }

  it should "be able to have penalties added" in {
    player.addPenaltyScore(3)
    player.penaltyScore should equal(3)
  }

  it should "not be able to add a negative penalty" in {
    intercept[IllegalArgumentException] {
      player.addPenaltyScore(-1)
    }
  }

  it should "have an initial score of 0" in {
    player.score should equal(0)
  }

  it should "be able to calculate its score for a player who joined for the first time 60 minutes ago" in {
    val now = DateTime.parse("2012-04-26T12:00:00.000-04:00")
    TimeFreezeService.freeze(now)
    player.joinedOn = now
    player.addBroken(1000)
    player.addPlaced(1000)
    player.addMinutesPlayed(60)
    player.penaltyScore = 5000

    TimeFreezeService.freeze(now.plusMinutes(60))
    player.calculateScore

    player.score should equal(115000)
  }
}
