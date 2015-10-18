package com.mobilechallenge.game.renderers;

import android.content.Context;
import android.opengl.GLSurfaceView;
import com.mobilechallenge.game.R;
import com.mobilechallenge.game.objects.Chip;
import com.mobilechallenge.game.objects.Deck;
import com.mobilechallenge.game.programs.DefaultColorProgram;
import com.mobilechallenge.game.programs.DefaultTextureProgram;
import com.mobilechallenge.game.utils.Geometry;
import com.mobilechallenge.game.utils.TextureHelper;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.invertM;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.multiplyMV;
import static android.opengl.Matrix.perspectiveM;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.setLookAtM;
import static android.opengl.Matrix.translateM;

/**
 * Project: Game
 * Date: 10/18/15
 * Code style: SquareAndroid (https://github.com/square/java-code-styles)
 */
public class GameRenderer implements GLSurfaceView.Renderer {
  @Override public void onSurfaceCreated(GL10 gl, EGLConfig config) {

  }

  @Override public void onSurfaceChanged(GL10 gl, int width, int height) {

  }

  @Override public void onDrawFrame(GL10 gl) {

  }

  /*private static final float LEFT_BOUND = -0.5f;
  private static final float RIGHT_BOUND = 0.5f;
  private static final float FAR_BOUND = -0.8f;
  private static final float NEAR_BOUND = 0.8f;

  private final Context mContext;

  private final float[] mProjectionMatrix = new float[16];
  private final float[] mModelMatrix = new float[16];
  private final float[] mViewMatrix = new float[16];
  private final float[] mViewProjectionMatrix = new float[16];
  private final float[] mModelViewProjectionMatrix = new float[16];
  private final float[] mInvertedViewProjectionMatrix = new float[16];

  private Deck mDeck;
  private Chip mChip;

  private DefaultTextureProgram mTextureProgram;
  private DefaultColorProgram mColorProgram;

  private int mTexture;

  private Geometry.Point mChipPosition;
  private Geometry.Point mPrevChipPosition; // this was needed to check speed vector (current-prev)

  private Geometry.Point mPuckPosition;
  private Geometry.Vector mPuckVector;

  public GameRenderer(Context context) {
    this.mContext = context;
  }

  @Override public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

    mDeck = new Deck();
    mChip = new Chip(0.08f, 0.05f, 32);

    mPuckPosition = new Geometry.Point(0f, mChip.height / 2f, 0f);
    mPuckVector = new Geometry.Vector(1f, 0f, 1f);

    mTextureProgram = new DefaultTextureProgram(mContext);
    mColorProgram = new DefaultColorProgram(mContext);

    mChipPosition = new Geometry.Point(0f, mChip.height / 2f, 0.4f);

    mTexture = TextureHelper.loadTexture(mContext,
        R.drawable.air_hockey_surface_512x512); // todo(tonyshkurenko), 10/18/15:  fix this
  }

  @Override public void onSurfaceChanged(GL10 gl, int width, int height) {
    glViewport(0, 0, width, height);

    perspectiveM(mProjectionMatrix, 0, 45, (float) width / (float) height, 1f, 10f);

    setLookAtM(mViewMatrix, 0, 0, 2, 0f, 0f, 0f, 0f, 0f, 0f, 0.0f);
  }

  @Override public void onDrawFrame(GL10 gl) {
    glClear(GL_COLOR_BUFFER_BIT); // clear screen

    multiplyMM(mViewProjectionMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
    invertM(mInvertedViewProjectionMatrix, 0, mViewProjectionMatrix, 0);

    mPuckPosition = mPuckPosition.translate(mPuckVector); // move puck with it's vector (speed)

    if (mPuckPosition.x < LEFT_BOUND + mChip.radius
        || mPuckPosition.x > RIGHT_BOUND - mChip.radius) {
      mPuckVector =
          new Geometry.Vector(-mPuckVector.x, mPuckVector.y, mPuckVector.z); // reflect speed vector
      mPuckVector = mPuckVector.scale(0.9f); // slow down
    }
    if (mPuckPosition.z < FAR_BOUND + mChip.radius || mPuckPosition.z > NEAR_BOUND - mChip.radius) {
      mPuckVector =
          new Geometry.Vector(mPuckVector.x, mPuckVector.y, -mPuckVector.z); // reflect speed vector
      mPuckVector = mPuckVector.scale(0.9f); // slow down
    }

    mPuckPosition = new Geometry.Point( // keep it inside the deck
        clamp(mPuckPosition.x, LEFT_BOUND + mChip.radius, RIGHT_BOUND - mChip.radius),
        mPuckPosition.y,
        clamp(mPuckPosition.z, FAR_BOUND + mChip.radius, NEAR_BOUND - mChip.radius));

    mPuckVector = mPuckVector.scale(0.99f); // slow down it

    positionDeckInTheScene();
    mTextureProgram.useProgram();
    mTextureProgram.setUniforms(mModelViewProjectionMatrix, mTexture);
    mDeck.bindData(mTextureProgram);
    mDeck.draw();

    positionObjectInScene(mChipPosition.x, mChipPosition.y, mChipPosition.z);
    mColorProgram.useProgram();
    mColorProgram.setUniforms(mModelViewProjectionMatrix, 0f, 0f, 1f);
    mChip.bindData(mColorProgram);
    mChip.draw();

    positionObjectInScene(mPuckPosition.x, mPuckPosition.y, mPuckPosition.z);
    mColorProgram.setUniforms(mModelViewProjectionMatrix, 0.8f, 0.8f, 1f);
    mChip.bindData(mColorProgram);
    mChip.draw();
  }

  private void positionObjectInScene(float x, float y, float z) {
    setIdentityM(mModelMatrix, 0);
    translateM(mModelMatrix, 0, x, y, z);
    multiplyMM(mModelViewProjectionMatrix, 0, mViewProjectionMatrix, 0, mModelMatrix, 0);
  }

  private void positionDeckInTheScene() {
    setIdentityM(mModelMatrix, 0);
    rotateM(mModelMatrix, 0, -90f, 1f, 0f, 0f); // rotate around x
    multiplyMM(mModelViewProjectionMatrix, 0, mViewProjectionMatrix, 0, mModelMatrix, 0);
  }

  *//**
   * Convert screen touch point to the ray to handle touches in openGL
   *
   * @param normalizedX x
   * @param normalizedY y
   * @return ray
   *//*
  private Geometry.Ray convertNormalized2DPointToRay(float normalizedX, float normalizedY) {

    final float[] nearPointNdc = { normalizedX, normalizedY, -1, 1 };
    final float[] farPointNdc = { normalizedX, normalizedY, 1, 1 };

    final float[] nearPointWorld = new float[4];
    final float[] farPointWorld = new float[4];

    multiplyMV(nearPointWorld, 0, mInvertedViewProjectionMatrix, 0, nearPointNdc, 0);
    multiplyMV(farPointWorld, 0, mInvertedViewProjectionMatrix, 0, farPointNdc, 0);

    divideByW(nearPointWorld);
    divideByW(farPointWorld);

    Geometry.Point nearPointRay =
        new Geometry.Point(nearPointWorld[0], nearPointWorld[1], nearPointWorld[2]);
    Geometry.Point farPointRay =
        new Geometry.Point(farPointWorld[0], farPointWorld[1], farPointWorld[2]);

    return new Geometry.Ray(nearPointRay, Geometry.vectorBetween(nearPointRay, farPointRay));
  }

  private void divideByW(float[] vector) {
    vector[0] /= vector[3];
    vector[1] /= vector[3];
    vector[2] /= vector[3];
  }

  *//**
   * That was for dragging, it tried to keep chip in the bounds of the table
   *
   * @param value dragged x/y
   * @param min min x/y
   * @param max max x/y
   * @return return x/y
   *//*
  private float clamp(float value, float min, float max) {
    return Math.min(max, Math.max(value, min));
  }*/
}
