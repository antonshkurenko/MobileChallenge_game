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
public class PointDeserializer implements JsonDeserializer<Geometry.Point> {
  @Override public Geometry.Point deserialize(JsonElement json, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {

    JsonObject o = (JsonObject) json;
    return new Geometry.Point(o.get("x").getAsFloat(), o.get("y").getAsFloat());
  }
}
