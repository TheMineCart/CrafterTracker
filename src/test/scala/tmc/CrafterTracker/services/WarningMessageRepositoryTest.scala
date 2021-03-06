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

  it should "return a list of ordered messages by most recent create time" in {
    val now = new DateTime

    TimeFreezeService.freeze(now.plusMinutes(1))
    repository.save(new WarningMessage("Jeremy", "Paul", "I heard you like tests.", Minor, 1000))

    TimeFreezeService.freeze(now.minusMinutes(3))
    repository.save(new WarningMessage("Jason", "Paul", "I heard you like tests.", Major, 1000))

    TimeFreezeService.freeze(now)
    repository.save(new WarningMessage("Sam", "Paul", "I heard you like tests.", Moderate, 1000))

    TimeFreezeService.unfreeze()

    val messages: List[WarningMessage] = repository.findByPlayerName("Paul")
    messages(0).sender should equal ("Jeremy")
    messages(1).sender should equal ("Sam")
    messages(2).sender should equal ("Jason")
  }

  it should "acknowledge a warning message and return true" in {
    TimeFreezeService.freeze()
    WarningMessageRepository.save(new WarningMessage("Sam", "Jason", "You bad", Major, 500))
    val result = WarningMessageRepository.acknowledgeWarningFor("Jason", new DateTime)

    result should equal (true)
    WarningMessageRepository.findByPlayerName("Jason")(0).acknowledged should equal (true)
    TimeFreezeService.unfreeze()
  }

  it should "not acknowledge a message and return false if recipient and time is wrong" in {
    WarningMessageRepository.save(new WarningMessage("Sam", "Jason", "You bad", Major, 500))
    var result = WarningMessageRepository.acknowledgeWarningFor("NotJason", new DateTime)

    result should equal (false)
    WarningMessageRepository.findByPlayerName("Jason")(0).acknowledged should equal (false)

    result = WarningMessageRepository.acknowledgeWarningFor("Jason", (new DateTime()).plusMinutes(1))
    result should equal (false)
    WarningMessageRepository.findByPlayerName("Jason")(0).acknowledged should equal (false)
  }

  it should "get the list of warnings a user has received after a certain date" in {
    var now = new DateTime
    TimeFreezeService.freeze(now.minusDays(1))
    WarningMessageRepository.save(new WarningMessage("Sam", "Jason", "warning message 1", Major, 500))
    TimeFreezeService.freeze(now.minusDays(2))
    WarningMessageRepository.save(new WarningMessage("Sam", "Bob", "warning message 2", Major, 500))
    TimeFreezeService.freeze(now.minusDays(3))
    WarningMessageRepository.save(new WarningMessage("Sam", "Jacob", "warning message 3", Major, 500))
    TimeFreezeService.freeze(now.minusDays(4))
    WarningMessageRepository.save(new WarningMessage("Sam", "Dan", "warning message 4", Major, 500))
    TimeFreezeService.unfreeze()

    val recentMessages = WarningMessageRepository.findMessagesSince(now.minusDays(3))
    recentMessages.size should equal (2)
    recentMessages(0).recipient should equal ("Jason")
    recentMessages(1).recipient should equal ("Bob")
  }

  it should "return an empty list if no warnings were created after a certain date" in {
    WarningMessageRepository.findMessagesSince(new DateTime).size should equal (0)
  }
}
