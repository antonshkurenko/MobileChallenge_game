package com.mobilechallenge.game.utils.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mobilechallenge.game.utils.Geometry;
import java.lang.reflect.Type;

/**
 * Project: Game
 * Date: 10/21/15
 * Code style: SquareAndroid (https://github.com/square/java-code-styles)
 *
 * this and other boilerplate is for working on some phones
 */
public class VectorSerializer implements JsonSerializer<Geometry.Vector> {

  @Override public JsonElement serialize(Geometry.Vector src, Type typeOfSrc,
      JsonSerializationContext context) {
    final JsonObject out = new JsonObject();
    out.addProperty("x", src.x);
    out.addProperty("y", src.y);
    out.addProperty("z", src.z);
    return out;
  }
}
