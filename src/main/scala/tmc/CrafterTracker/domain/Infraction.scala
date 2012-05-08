package tmc.CrafterTracker.domain

import com.google.gson.annotations.Expose

// Created by cyrus on 5/8/12 at 1:39 PM

trait Infraction {
  @Expose val penalty: Float
}

object Minor extends Infraction {
  val penalty = 0.1
}

object Moderate extends Infraction {
  val penalty = 0.25
}

object Major extends Infraction {
  val penalty = 0.5
}
