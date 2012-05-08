package tmc.CrafterTracker.adapters

import java.lang.reflect.Type
import com.google.gson._
import tmc.CrafterTracker.domain.{Major, Moderate, Minor, Infraction}

// Created by cyrus on 5/8/12 at 2:46 PM

class InfractionAdapter extends JsonSerializer[Infraction] with JsonDeserializer[Infraction]{
  def serialize(infraction: Infraction, p2: Type, p3: JsonSerializationContext) = {
    new JsonPrimitive(
      infraction match {
        case Minor => "MINOR"
        case Moderate => "MODERATE"
        case Major => "MAJOR"
      }
    )
  }

  def deserialize(json: JsonElement, t: Type, context: JsonDeserializationContext) = {
    json.getAsString match {
      case "MINOR" => Minor
      case "MODERATE" => Moderate
      case "MAJOR" => Major
    }
  }
}
