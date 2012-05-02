package tmc.CrafterTracker.domain

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.joda.time.DateTime
import tmc.BukkitTestUtilities.Services.TimeFreezeService

// Created by cyrus on 5/1/12 at 12:05 PM

class SessionTest extends FlatSpec with ShouldMatchers {

  it should "have a name" in {
    var session = new Session("Sam")

    session.username should equal ("Sam")
  }

  it should "have connectedAt now" in {
    TimeFreezeService.freeze()
    var session = new Session("Sam")

    session.connectedAt should equal (new DateTime())
    TimeFreezeService.unfreeze()
  }

  it should "have disconnectedAt now" in {
    TimeFreezeService.freeze()
    var session = new Session("Sam")

    session.disconnected

    session.disconnectedAt should equal (new DateTime())
    TimeFreezeService.unfreeze()
  }

  it should "Count blocks broken" in {
    var session = new Session("Jason733i")

    session.blocksBroken should equal (0)
    session.blockBroken
    session.blocksBroken should equal (1)
  }

  it should "Count blocks placed" in {
    var session = new Session("Jason733i")

    session.blocksPlaced should equal (0)
    session.blockPlaced
    session.blocksPlaced should equal (1)
  }
}
