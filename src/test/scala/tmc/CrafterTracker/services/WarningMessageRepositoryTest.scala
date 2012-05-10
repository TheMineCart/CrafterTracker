package tmc.CrafterTracker.services

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{FlatSpec, BeforeAndAfterEach}
import tmc.CrafterTracker.domain.{Moderate, Major, Minor, WarningMessage}
import org.joda.time.DateTime
import tmc.BukkitTestUtilities.Services.{TimeFreezeService, RepositoryTest}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

// Created by cyrus on 5/8/12 at 1:58 PM

@RunWith(classOf[JUnitRunner])
class WarningMessageRepositoryTest extends RepositoryTest with FlatSpec with ShouldMatchers with BeforeAndAfterEach {

  WarningMessageRepository.collection = getCollection("WarningMessages")
  val repository = WarningMessageRepository

  override def afterEach() {
    clearTestData()
  }

  it should "save and find a WarningMessage by playerName" in {
    var message = new WarningMessage("Sam", "Jason", "You're a major developer", Major, 1000)
    repository.save(message)

    repository.findByPlayerName("Jason").head.recipient should equal ("Jason")
    repository.findByPlayerName("Jason").head.infraction should equal (Major)
    repository.findByPlayerName("Jason").head.text should equal ("You're a major developer")
  }

  it should "calculate the penalty score upon creation" in {
    var currentPlayerScore: Long = 1000
    var message = new WarningMessage("S", "J", "You're Minorly Bad", Minor, currentPlayerScore)
    message.score should equal (100)

    currentPlayerScore = 1000
    message = new WarningMessage("S", "J", "You're Minorly Bad", Moderate, currentPlayerScore)
    message.score should equal (250)

    currentPlayerScore = 1000
    message = new WarningMessage("S", "J", "You're Minorly Bad", Major, currentPlayerScore)
    message.score should equal (500)
  }

  it should "return an empty list if there are no warning messages for playerName" in {
    repository.findByPlayerName("Jason") should equal (Nil)
  }

  it should "return a list of all unacknowledged messages" in {
    repository.save(new WarningMessage("Jason", "Sam", "I heard you like alcohol.", Minor, 1000))

    var message = new WarningMessage("Jason", "Sam", "So I gave you a bar.", Moderate, 1000)
    message.acknowledge
    repository.save(message)

    val unacknowledgedMsgs: List[WarningMessage] = repository.findUnacknowledgedByPlayerName("Sam")
    unacknowledgedMsgs.size should equal (1)
    unacknowledgedMsgs.head.infraction should equal (Minor)
  }

  it should "return a list of ordered messages" in {
    val now = new DateTime

    TimeFreezeService.freeze(now.plusMinutes(1))
    repository.save(new WarningMessage("Jeremy", "Paul", "I heard you like tests.", Minor, 1000))

    TimeFreezeService.freeze(now.minusMinutes(3))
    repository.save(new WarningMessage("Jason", "Paul", "I heard you like tests.", Major, 1000))

    TimeFreezeService.freeze(now)
    repository.save(new WarningMessage("Sam", "Paul", "I heard you like tests.", Moderate, 1000))

    TimeFreezeService.unfreeze()

    val messages: List[WarningMessage] = repository.findByPlayerName("Paul")
    messages(0).sender should equal ("Jason")
    messages(1).sender should equal ("Sam")
    messages(2).sender should equal ("Jeremy")
  }
}
