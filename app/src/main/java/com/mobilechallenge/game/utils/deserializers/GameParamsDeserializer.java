package com.mobilechallenge.game.utils.deserializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.mobilechallenge.game.controllers.GameMechanics;
import com.mobilechallenge.game.utils.Geometry;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Project: Game
 * Date: 10/21/15
 * Code style: SquareAndroid (https://github.com/square/java-code-styles)
 *
 * this and other boilerplate is for working on some phones
 */
public class GameParamsDeserializer implements JsonDeserializer<GameMechanics.GameParams> {

  @Override public GameMechanics.GameParams deserialize(JsonElement json, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {

    final JsonObject o = (JsonObject) json;

    final Type point = new TypeToken<Geometry.Point>() {}.getType();
    final Type vector = new TypeToken<Geometry.Vector>() {}.getType();
    final Type pointList = new TypeToken<List<Geometry.Point>>() {}.getType();
    final Type vectorList = new TypeToken<List<Geometry.Vector>>() {}.getType();

    return new GameMechanics.GameParams.Builder().setAcceleration(
        o.get("mAcceleration").getAsFloat())
        .setAspectRatio(o.get("mAspectRatio").getAsFloat())
        .setDifficultyLevel(o.get("mDifficultyLevel").getAsInt())
        .setMaxSpeed(o.get("mMaxSpeed").getAsFloat())
        .setTimePassed(o.get("mTimePassed").getAsLong())
        .setChipPosition(
            context.deserialize(o.get("mChipPosition").getAsJsonObject(), point))
        .setChipSpeed(
            context.deserialize(o.get("mChipSpeed").getAsJsonObject(), vector))
        .setStartEnemyVector(context.deserialize(o.get("mStartEnemyVector").getAsJsonObject(), vector))
        .setEnemyPositions(context.deserialize(o.get("mEnemyPositions").getAsJsonArray(), pointList))
        .setEnemyVectors(context.deserialize(o.get("mEnemyVectors").getAsJsonArray(), vectorList))
        .build();
  }
}
