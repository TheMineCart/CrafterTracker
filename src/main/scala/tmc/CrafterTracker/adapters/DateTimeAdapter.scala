// Copyright (C) 2012 Cyrus Innovation
package tmc.CrafterTracker.adapters

import org.joda.time.DateTime
import java.lang.reflect.Type
import com.google.gson._
import java.util.Date

// Created by cyrus on 5/4/12 at 2:53 PM

object DateTimeAdapter extends JsonSerializer[DateTime] with JsonDeserializer[DateTime] {

  override def serialize(src: DateTime, typeOfSrc: Type, context: JsonSerializationContext) = {
    new JsonPrimitive(src.toString())
  }

  override def deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext) = {
    try {
      new DateTime(json.getAsString)
    }
    catch {
      case e:IllegalArgumentException => new DateTime(context.deserialize(json, classOf[Date]).asInstanceOf[Date])
    }
  }
}
