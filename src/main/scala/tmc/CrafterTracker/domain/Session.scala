// Copyright (C) 2012 Cyrus Innovation
package tmc.CrafterTracker.domain

import org.joda.time.DateTime
import com.google.gson.annotations.Expose

// Created by cyrus on 5/1/12 at 11:52 AM

class Session(u: String, a: String) {
  @Expose val username: String = u;
  @Expose val ipAddress: String = a.stripPrefix("/")
  @Expose var blocksBroken: Int = 0;
  @Expose var blocksPlaced: Int = 0;
  @Expose var connectedAt: DateTime = new DateTime;
  @Expose var disconnectedAt: DateTime = null;

  def blockBroken {
    blocksBroken += 1
  }

  def blockPlaced {
    blocksPlaced += 1
  }

  def disconnected {
    disconnectedAt = new DateTime
  }
}
