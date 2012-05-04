package tmc.CrafterTracker.services

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import tmc.CrafterTracker.domain.Session
import tmc.BukkitTestUtilities.Services.RepositoryTest

// Created by cyrus on 5/4/12 at 2:00 PM

class SessionRepositoryTest extends RepositoryTest with FlatSpec with ShouldMatchers with BeforeAndAfterEach {
  var repository: SessionRepository = null

  override def beforeEach() {
    repository = new SessionRepository(getCollection("Sessions"));
  }

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

}
