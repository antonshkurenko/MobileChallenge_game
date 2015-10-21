package com.mobilechallenge.game.utils.serializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mobilechallenge.game.controllers.GameMechanics;
import com.mobilechallenge.game.utils.Geometry;
import java.lang.reflect.Type;

/**
 * Project: Game
 * Date: 10/21/15
 * Code style: SquareAndroid (https://github.com/square/java-code-styles)
 *
 * this and other boilerplate is for working on some phones
 */
public class GameParamsSerializer implements JsonSerializer<GameMechanics.GameParams> {

  @Override public JsonElement serialize(GameMechanics.GameParams src, Type typeOfSrc,
      JsonSerializationContext context) {

    final JsonObject params = new JsonObject();

    params.addProperty("mAcceleration", src.getAcceleration());
    params.addProperty("mAspectRatio", src.getAspectRatio());
    params.addProperty("mDifficultyLevel", src.getDifficultyLevel());
    params.addProperty("mMaxSpeed", src.getMaxSpeed());
    params.addProperty("mTimePassed", src.getTimePassed());

    params.add("mChipPosition", context.serialize(src.getChipPosition()));
    params.add("mChipSpeed", context.serialize(src.getChipSpeed()));
    params.add("mStartEnemyVector", context.serialize(src.getStartEnemyVector()));

    JsonArray positions = new JsonArray();
    params.add("mEnemyPositions", positions);
    for(Geometry.Point point : src.getEnemyPositions()) {
      positions.add(context.serialize(point));
    }

    JsonArray vectors = new JsonArray();
    params.add("mEnemyVectors", vectors);
    for(Geometry.Vector vector : src.getEnemyVectors()) {
      vectors.add(context.serialize(vector));
    }

    return params;
  }
}
