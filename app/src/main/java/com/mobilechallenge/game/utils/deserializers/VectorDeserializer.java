package com.mobilechallenge.game.utils.deserializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mobilechallenge.game.utils.Geometry;
import java.lang.reflect.Type;

/**
 * Project: Game
 * Date: 10/21/15
 * Code style: SquareAndroid (https://github.com/square/java-code-styles)
 *
 * this and other boilerplate is for working on some phones
 */
public class VectorDeserializer implements JsonDeserializer<Geometry.Vector> {
  @Override public Geometry.Vector deserialize(JsonElement json, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {

    final JsonObject o = (JsonObject)json;
    return new Geometry.Vector(o.get("x").getAsFloat(), o.get("y").getAsFloat());
  }
}
