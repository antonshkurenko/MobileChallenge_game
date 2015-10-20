package com.mobilechallenge.game.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.DrawableRes;
import timber.log.Timber;

import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_LINEAR_MIPMAP_LINEAR;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glGenerateMipmap;
import static android.opengl.GLES20.glTexParameteri;
import static android.opengl.GLUtils.texImage2D;

/**
 * Project: Game
 * Date: 10/18/15
 * Code style: SquareAndroid (https://github.com/square/java-code-styles)
 */
public class TextureHelper {

  public static int loadTexture(Context context, int resourceId) {
    final int[] textureObjectIds = new int[1];
    glGenTextures(1, textureObjectIds, 0);

    if (textureObjectIds[0] == 0) {
      Timber.w("Could not generate a new OpenGL texture object.");
      return 0;
    }

    final BitmapFactory.Options options = new BitmapFactory.Options();
    options.inScaled = false;

    final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

    if (bitmap == null) {
      Timber.w("Resource ID " + resourceId + " could not be decoded.");

      glDeleteTextures(1, textureObjectIds, 0);
      return 0;
    }

    glBindTexture(GL_TEXTURE_2D, textureObjectIds[0]);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

    texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
    bitmap.recycle();

    glGenerateMipmap(GL_TEXTURE_2D);

    return textureObjectIds[0];
  }

  public static int[] loadTextures(Context ctx, @DrawableRes int... resourceIds) {

    if (resourceIds.length == 0) {
      throw new IllegalArgumentException("You didn't passed resourceIds for textures");
    }

    final int[] textureObjectIds = new int[resourceIds.length];
    glGenTextures(resourceIds.length, textureObjectIds, 0);

    for (int i = 0; i < textureObjectIds.length; i++) {
      if (textureObjectIds[i] == 0) {
        Timber.w("Could not generate a new OpenGL texture object.");
        return null;
      }

      final BitmapFactory.Options options = new BitmapFactory.Options();
      options.inScaled = false;

      final Bitmap bitmap =
          BitmapFactory.decodeResource(ctx.getResources(), resourceIds[i], options);

      if (bitmap == null) {
        Timber.w("Resource ID %d could not be decoded.", resourceIds[i]);

        glDeleteTextures(1, textureObjectIds, i);
        return null;
      }

      glBindTexture(GL_TEXTURE_2D, textureObjectIds[i]);

      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

      texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
      bitmap.recycle();

      glGenerateMipmap(GL_TEXTURE_2D);
    }
    return textureObjectIds;
  }

  public static float[] getTextureVertices(float[] vertices) {
    final float[] textureVertices = new float[vertices.length];

    for (int i = 0; i < vertices.length; i++) {
      textureVertices[i] = (vertices[i] + 1) * 0.5f;
      Timber.d("Converting %f to %f.", vertices[i], textureVertices[i]);
    }
    return textureVertices;
  }
}
