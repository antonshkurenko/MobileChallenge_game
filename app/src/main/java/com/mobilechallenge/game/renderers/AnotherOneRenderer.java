package com.mobilechallenge.game.renderers;

import android.content.Context;
import android.opengl.GLSurfaceView;
import com.mobilechallenge.game.objects.Chip;
import com.mobilechallenge.game.objects.Deck;
import com.mobilechallenge.game.programs.SimpleSingleColorShaderProgram;
import com.mobilechallenge.game.programs.SimpleVaryingColorShaderProgram;
import com.mobilechallenge.game.utils.Geometry;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import timber.log.Timber;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.orthoM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

/**
 * Project: Game
 * Date: 10/18/15
 * Code style: SquareAndroid (https://github.com/square/java-code-styles)
 */
public class AnotherOneRenderer implements GLSurfaceView.Renderer {

  private final Context mContext;

  // @formatter:off
    private final float[] mProjectionMatrix = new float[16];
   private final float[  ] mModelMatrix = new float[16];
  private final float[    ] mModelProjectionMatrix = new float[16];
  // @formatter:on

  private float mAspectRatio; // it's > 1, w/h or h/w

  private SimpleVaryingColorShaderProgram mVaryingColorProgram;
  private SimpleSingleColorShaderProgram mSingleColorProgram;

  private Deck mDeck;
  private Chip mChip;

  private Geometry.Point mChipPosition; // position
  private Geometry.Vector mChipVector; // speed

  public AnotherOneRenderer(Context ctx) {
    mContext = ctx;
  }

  @Override public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

    mVaryingColorProgram = new SimpleVaryingColorShaderProgram(mContext);
    mSingleColorProgram = new SimpleSingleColorShaderProgram(mContext);
    mDeck = new Deck(); // since it doesn't need aspect ratio, it's initialized here
  }

  @Override public void onSurfaceChanged(GL10 gl, int width, int height) {
    glViewport(0, 0, width, height);

    mAspectRatio = width > height ? (float) width / (float) height : (float) height / (float) width;

    Timber.i("Width is %d, height is %d, aspect is %f", width, height, mAspectRatio);

    mChip = new Chip(0.15f, 32);

    mChipPosition = new Geometry.Point(0f, 0f, 0f);
    mChipVector = new Geometry.Vector(0.005f, 0f, 0f);

    /*if (width > height) {
      // In debug w is 1794, h is 1005, aspect 1.785075
      // Landscape
      orthoM(mProjectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f);
    } else {
      // Portrait or square
      orthoM(mProjectionMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f);
    }*/
    orthoM(mProjectionMatrix, 0, -1f, 1f, -1f, 1f, -1f, 1f);
  }

  @Override public void onDrawFrame(GL10 gl) {
    glClear(GL_COLOR_BUFFER_BIT);

    mChipPosition = mChipPosition.translate(mChipVector);

    mVaryingColorProgram.useProgram();
    mDeck.bindData(mVaryingColorProgram);
    mVaryingColorProgram.setUniforms(mProjectionMatrix);
    mDeck.draw();

    mSingleColorProgram.useProgram();
    mChip.bindData(mSingleColorProgram);
    positionObjectInScene(mChipPosition.x, mChipPosition.y);
    mSingleColorProgram.setUniforms(mModelProjectionMatrix, 1f, 0f, 0f);
    mChip.draw();
  }

  private void positionObjectInScene(float x, float y) {
    setIdentityM(mModelMatrix, 0);
    translateM(mModelMatrix, 0, x, y, 0f);
    multiplyMM(mModelProjectionMatrix, 0, mProjectionMatrix, 0, mModelMatrix, 0);
  }
}
