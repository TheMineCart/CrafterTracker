package tmc.CrafterTracker.listener

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import tmc.BukkitTestUtilities.Services.RepositoryTest
import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import tmc.CrafterTracker.services.WarningMessageRepository
import org.bukkit.event.player.PlayerChatEvent
import tmc.BukkitTestUtilities.Mocks.TestPlayer
import org.bukkit.ChatColor
import tmc.CrafterTracker.domain.{Minor, WarningMessage}

@RunWith(classOf[JUnitRunner])
class PlayerChatListenerTest extends RepositoryTest with FlatSpec with ShouldMatchers with BeforeAndAfterEach {

  WarningMessageRepository.collection = getCollection("WarningMessages")

  override def afterEach() {
    clearTestData()
  }

  it should "mute a player that has any unacknowledged warnings" in {
    WarningMessageRepository.save(new WarningMessage("Sam", "Jason", "you're a bad person", Minor, 1000L))
    val jason: TestPlayer = new TestPlayer("Jason")
    val event: PlayerChatEvent = new PlayerChatEvent(jason, "I like talking when I have unacknowledged warnings!")
    PlayerChatListener.mutePlayersWithWarnings(event)

    jason.getMessage should equal (ChatColor.DARK_RED + "Sorry, but you are muted until you acknowledge your warnings!")
  }

  it should "not mute a player who has no unacknowledged warnings" in {
    val acknowledged: WarningMessage = new WarningMessage("Sam", "Jason", "you're a bad person", Minor, 1000L)
    acknowledged.acknowledge
    WarningMessageRepository.save(acknowledged)
    val jason: TestPlayer = new TestPlayer("Jason")
    val event: PlayerChatEvent = new PlayerChatEvent(jason, "I like talking after I have acknowledged my warnings!")
    PlayerChatListener.mutePlayersWithWarnings(event)

    jason.getMessage should equal (null)
  }
}
