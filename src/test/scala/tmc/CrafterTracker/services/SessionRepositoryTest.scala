package tmc.CrafterTracker.services

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import tmc.CrafterTracker.domain.Session
import tmc.BukkitTestUtilities.Services.RepositoryTest
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

// Created by cyrus on 5/4/12 at 2:00 PM

@RunWith(classOf[JUnitRunner])
class SessionRepositoryTest extends RepositoryTest with FlatSpec with ShouldMatchers with BeforeAndAfterEach {

  SessionRepository.collection = getCollection("Sessions");
  val repository = SessionRepository

  override def afterEach() {
    clearTestData()
  }

  it should "Save a session to the database" in {
    var session = new Session("Jason", "127.0.0.1")

    session.blockBroken
    session.blockPlaced
    session.disconnectedAt

    repository.save(session)

    var jasonSessions: List[Session] = repository.findByPlayerName("Jason")

    jasonSessions.head.username should equal ("Jason")
    jasonSessions.size should equal (1)
  }

  it should "not retrieve sessions not belonging to the player" in {
    repository.save(new Session("Jason", "127.0.0.1"))
    repository.save(new Session("Brian", "127.0.0.1"))
    repository.save(new Session("Sam", "127.0.0.1"))
    repository.save(new Session("Paul", "127.0.0.1"))
    repository.save(new Session("Sam", "127.0.0.1"))

    var jasonSessions: List[Session] = repository.findByPlayerName("Sam")

    jasonSessions.head.username should equal ("Sam")
    jasonSessions.size should equal (2)
  }

  it should "order sessions by most recent connectedAt" in {
    var session: Session = new Session("Jason", "127.0.0.1")
    val now: DateTime = new DateTime()

    session.connectedAt = now
    repository.save(session)
    session.connectedAt = now.plusMinutes(10)
    repository.save(session)
    session.connectedAt = now.plusMinutes(5)
    repository.save(session)
    session.connectedAt = now.minusMinutes(4)
    repository.save(session)

    repository.count should equal (4)

    val sessions: List[Session] = repository.findByPlayerName("Jason")

    sessions(0).connectedAt should equal (now.plusMinutes(10))
    sessions(1).connectedAt should equal (now.plusMinutes(5))
    sessions(2).connectedAt should equal (now)
    sessions(3).connectedAt should equal (now.minusMinutes(4))

    repository.findMostRecentByPlayerName("Jason").connectedAt should equal (now.plusMinutes(10))
  }

  it should "return null if there is no session for a playerName" in {
    repository.findMostRecentByPlayerName("Jason") should equal (null)
  }

}
