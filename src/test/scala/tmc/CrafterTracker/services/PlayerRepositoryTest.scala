package tmc.CrafterTracker.services

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.BeforeAndAfterEach
import tmc.CrafterTracker.domain.Player
import tmc.BukkitTestUtilities.Services.{TimeFreezeService, RepositoryTest}
import org.joda.time.DateTime

// Created by cyrus on 5/9/12 at 9:48 AM

class PlayerRepositoryTest extends RepositoryTest with FlatSpec with ShouldMatchers with BeforeAndAfterEach {
  var repository: PlayerRepository = null

  override def beforeEach() {
    repository = new PlayerRepository(getCollection("Players"))
  }

  override def afterEach() {
    clearTestData()
  }

  it should "save a new player" in {
    TimeFreezeService.freeze()
    repository.save(new Player("Sam"))

    repository.findByPlayerName("Sam").username should equal ("Sam")
    repository.findByPlayerName("Sam").joinedOn should equal (new DateTime)
    repository.exists("Sam") should equal (true)

    TimeFreezeService.unfreeze()
  }

  it should "not find a player that does not exist" in {
    repository.findByPlayerName("Sam") should equal (null)
    repository.exists("Sam") should equal (false)
  }

  it should "not create a second player object for the same player" in {
    val sam: Player = new Player("Sam")
    repository.save(sam)
    repository.count() should equal (1)

    sam.addBroken(20)

    repository.save(sam)
    repository.count() should equal (1)
    val newSam: Player = repository.findByPlayerName("Sam")

    newSam.blocksBroken should equal (20)
    newSam.joinedOn should equal (sam.joinedOn)
  }
}
