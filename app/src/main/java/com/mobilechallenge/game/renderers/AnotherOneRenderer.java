package com.mobilechallenge.game.renderers;

import android.content.Context;
import android.opengl.GLSurfaceView;
import com.mobilechallenge.game.objects.Deck;
import com.mobilechallenge.game.programs.SimpleShaderProgram;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glViewport;

/**
 * Project: Game
 * Date: 10/18/15
 * Code style: SquareAndroid (https://github.com/square/java-code-styles)
 */
public class AnotherOneRenderer implements GLSurfaceView.Renderer {

  private final Context mContext;

  private SimpleShaderProgram mSimpleShaderProgram;

  private Deck mDeck;

  public AnotherOneRenderer(Context ctx) {
    mContext = ctx;

  }

  @Override public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

    mSimpleShaderProgram = new SimpleShaderProgram(mContext);
    mDeck = new Deck();

    mSimpleShaderProgram.useProgram();
    mDeck.bindData(mSimpleShaderProgram);
  }

  @Override public void onSurfaceChanged(GL10 gl, int width, int height) {
    glViewport(0, 0, width, height);
  }

  @Override public void onDrawFrame(GL10 gl) {
    glClear(GL_COLOR_BUFFER_BIT);

    mSimpleShaderProgram.setUniforms(1f, 1f, 1f);
    mDeck.draw();
  }
}
