package tmc.CrafterTracker.domain


// Created by cyrus on 5/8/12 at 1:39 PM

trait Infraction {
  val penalty: Float
}

object Minor extends Infraction {
  val penalty = 0.1F

  override def toString = {"minor"}
}

object Moderate extends Infraction {
  val penalty = 0.25F

  override def toString = {"moderate"}
}

object Major extends Infraction {
  val penalty = 0.5F

  override def toString = {"major"}
}
