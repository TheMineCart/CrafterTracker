package tmc.CrafterTracker.services

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{FlatSpec, BeforeAndAfterEach}
import tmc.CrafterTracker.domain.{Moderate, Major, Minor, WarningMessage}
import org.joda.time.DateTime
import tmc.BukkitTestUtilities.Services.{TimeFreezeService, RepositoryTest}

// Created by cyrus on 5/8/12 at 1:58 PM

class WarningMessageRepositoryTest extends RepositoryTest with FlatSpec with ShouldMatchers with BeforeAndAfterEach {
  val repository: WarningMessageRepository = new WarningMessageRepository(getCollection("WarningMessages"))

  override def afterEach() {
    clearTestData()
  }

  it should "save and find a WarningMessage by playerName" in {
    var message = new WarningMessage("Sam", "Jason", "You're a major developer", Major)
    repository.save(message)

    repository.findByPlayerName("Jason").head.recipient should equal ("Jason")
    repository.findByPlayerName("Jason").head.infraction should equal (Major)
    repository.findByPlayerName("Jason").head.text should equal ("You're a major developer")
  }

  it should "return an empty list if there are no warning messages for playerName" in {
    repository.findByPlayerName("Jason") should equal (Nil)
  }

  it should "return a list of all unacknowledged messages" in {
    repository.save(new WarningMessage("Jason", "Sam", "I heard you like alcohol.", Minor))

    var message = new WarningMessage("Jason", "Sam", "So I gave you a bar.", Moderate)
    message.acknowledge
    repository.save(message)

    val unacknowledgedMsgs: List[WarningMessage] = repository.findUnacknowledgedByPlayerName("Sam")
    unacknowledgedMsgs.size should equal (1)
    unacknowledgedMsgs.head.infraction should equal (Minor)
  }

  it should "return a list of ordered messages" in {
    val now = new DateTime

    TimeFreezeService.freeze(now.plusMinutes(1))
    repository.save(new WarningMessage("Jeremy", "Paul", "I heard you like tests.", Minor))

    TimeFreezeService.freeze(now.minusMinutes(3))
    repository.save(new WarningMessage("Jason", "Paul", "I heard you like tests.", Major))

    TimeFreezeService.freeze(now)
    repository.save(new WarningMessage("Sam", "Paul", "I heard you like tests.", Moderate))

    TimeFreezeService.unfreeze()

    val messages: List[WarningMessage] = repository.findByPlayerName("Paul")
    messages(0).sender should equal ("Jason")
    messages(1).sender should equal ("Sam")
    messages(2).sender should equal ("Jeremy")
  }

}
