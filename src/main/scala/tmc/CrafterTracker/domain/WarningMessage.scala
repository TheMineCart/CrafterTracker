package tmc.CrafterTracker.domain

import com.google.gson.annotations.Expose
import org.joda.time.DateTime

// Created by cyrus on 5/4/12 at 5:28 PM

class WarningMessage(r: String, t: String, i: Infraction) {
  @Expose val recipient: String = r
  @Expose val text: String = t
  @Expose val issuedAt: DateTime = new DateTime()
  @Expose val infraction: Infraction = i
  @Expose var score: Long = 0
  @Expose var acknowledged: Boolean = false
}


