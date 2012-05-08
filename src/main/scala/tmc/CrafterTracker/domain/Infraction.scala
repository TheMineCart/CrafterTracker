package tmc.CrafterTracker.domain


// Created by cyrus on 5/8/12 at 1:39 PM

trait Infraction {
  val penalty: Float
}

object Minor extends Infraction {
  val penalty = 0.1F
}

object Moderate extends Infraction {
  val penalty = 0.25F
}

object Major extends Infraction {
  val penalty = 0.5F
}
