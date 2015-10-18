package com.mobilechallenge.game.renderers;

import android.content.Context;
import android.opengl.GLSurfaceView;
import com.mobilechallenge.game.programs.ColorProgram;
import com.mobilechallenge.game.programs.TextureProgram;
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

  private final Context mContext;

  private final float [] mProjectionMatrix = new float[16];
  private final float [] mModelMatrix = new float[16];
  private final float [] mViewMatrix = new float[16];
  private final float [] mViewProjectionMatrix = new float[16];
  private final float [] mModelViewProjectionMatrix = new float[16];
  private final float [] mInvertedViewProjectionMatrix = new float[16];

  private Table mTable;
  private Mallet mMallet;
  private Puck mPuck;

  private TextureProgram mTextureProgram;
  private ColorProgram mColorProgram;

  private int mTexture;

  private boolean mMalletPressed = false;
  private Geometry.Point mBlueMalletPosition;
  private Geometry.Point mPrevBlueMalletPosition;

  private Geometry.Point mPuckPosition;
  private Geometry.Vector mPuckVector;

  private final float LEFT_BOUND = -0.5f;
  private final float RIGHT_BOUND = 0.5f;
  private final float FAR_BOUND = -0.8f;
  private final float NEAR_BOUND = 0.8f;

  public GameRenderer(Context context) {
    this.mContext = context;
  }

  @Override
  public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

    mTable = new Table();
    mMallet = new Mallet(0.08f, 0.15f, 32);
    mPuck = new Puck(0.06f, 0.02f, 32);

    mPuckPosition = new Geometry.Point(0f, mPuck.height / 2f, 0f);
    mPuckVector = new Geometry.Vector(0f, 0f, 0f);

    mTextureProgram = new TextureShaderProgram(mContext);
    mColorProgram = new ColorShaderProgram(mContext);

    mBlueMalletPosition =
        new Geometry.Point(0f, mMallet.height / 2f, 0.4f);

    mTexture = TextureHelper
        .loadTexture(mContext, R.drawable.air_hockey_surface_512x512);
  }

  @Override
  public void onSurfaceChanged(GL10 gl, int width, int height) {
    glViewport(0 ,0 , width, height);

    MatrixHelper.perspectiveM(mProjectionMatrix, 45,
        (float) width / (float) height, 1f, 10f);

    setLookAtM(mViewMatrix, 0, 0f, 1.2f, 2.2f, 0f, 0f, 0f, 0f, 1f, 0f);
  }

  @Override
  public void onDrawFrame(GL10 gl) {
    glClear(GL_COLOR_BUFFER_BIT);

    multiplyMM(mViewProjectionMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
    invertM(mInvertedViewProjectionMatrix, 0, mViewProjectionMatrix, 0);

    mPuckPosition = mPuckPosition.translate(mPuckVector);

    if(mPuckPosition.x < LEFT_BOUND + mPuck.radius ||
        mPuckPosition.x > RIGHT_BOUND - mPuck.radius) {
      mPuckVector = new Geometry.Vector(-mPuckVector.x,
          mPuckVector.y, mPuckVector.z);
      mPuckVector = mPuckVector.scale(0.9f);
    }
    if(mPuckPosition.z < FAR_BOUND + mPuck.radius ||
        mPuckPosition.z > NEAR_BOUND - mPuck.radius) {
      mPuckVector = new Geometry.Vector(mPuckVector.x,
          mPuckVector.y, -mPuckVector.z);
      mPuckVector = mPuckVector.scale(0.9f);
    }

    mPuckPosition = new Geometry.Point(
        clamp(mPuckPosition.x, LEFT_BOUND + mPuck.radius,
            RIGHT_BOUND - mPuck.radius),
        mPuckPosition.y,
        clamp(mPuckPosition.z, FAR_BOUND + mPuck.radius,
            NEAR_BOUND - mPuck.radius)
    );

    mPuckVector = mPuckVector.scale(0.99f);

    positionTableInTheScene();
    mTextureProgram.useProgram();
    mTextureProgram.setUniforms(mModelViewProjectionMatrix, mTexture);
    mTable.bindData(mTextureProgram);
    mTable.draw();

    positionObjectInScene(0f, mMallet.height / 2f, -0.4f);
    mColorProgram.useProgram();
    mColorProgram.setUniforms(mModelViewProjectionMatrix, 1f, 0f, 0f);
    mMallet.bindData(mColorProgram);
    mMallet.draw();

    positionObjectInScene(mBlueMalletPosition.x,
        mBlueMalletPosition.y, mBlueMalletPosition.z);
    mColorProgram.setUniforms(mModelViewProjectionMatrix, 0f, 0f, 1f);
    mMallet.draw();

    positionObjectInScene(mPuckPosition.x,
        mPuckPosition.y, mPuckPosition.z);
    mColorProgram.setUniforms(mModelViewProjectionMatrix, 0.8f, 0.8f, 1f);
    mPuck.bindData(mColorProgram);
    mPuck.draw();
  }

  private void positionObjectInScene(float x, float y, float z) {
    setIdentityM(mModelMatrix, 0);
    translateM(mModelMatrix, 0, x, y, z);
    multiplyMM(mModelViewProjectionMatrix, 0, mViewProjectionMatrix,
        0, mModelMatrix, 0);
  }

  private void positionTableInTheScene() {
    setIdentityM(mModelMatrix, 0);
    rotateM(mModelMatrix, 0, -90f, 1f, 0f, 0f);
    multiplyMM(mModelViewProjectionMatrix,
        0, mViewProjectionMatrix, 0, mModelMatrix, 0);
  }

  public void handleTouchPress(float normalizedX, float normalizedY) {
    Geometry.Ray ray = convertNormalized2DPointToRay(normalizedX, normalizedY);

    Geometry.Sphere malletBoundingSphere = new Geometry.Sphere(new Geometry.Point(
        mBlueMalletPosition.x,
        mBlueMalletPosition.y,
        mBlueMalletPosition.z),
        mMallet.height / 2f
    );
    mMalletPressed = Geometry.intersects(malletBoundingSphere, ray);
  }

  public void handleTouchDrag(float normalizedX, float normalizedY) {
    if(mMalletPressed) {
      Geometry.Ray ray = convertNormalized2DPointToRay(normalizedX, normalizedY);
      Geometry.Plane plane = new Geometry.Plane(new Geometry.Point(0, 0, 0), new Geometry.Vector(0, 1, 0));

      Geometry.Point touchedPoint = Geometry.intersectionPoint(ray, plane);

      mPrevBlueMalletPosition = mBlueMalletPosition;

      mBlueMalletPosition = new Geometry.Point(
          clamp(touchedPoint.x,
              LEFT_BOUND + mMallet.radius,
              RIGHT_BOUND - mMallet.radius),
          mMallet.height / 2f,
          clamp(touchedPoint.z,
              0f + mMallet.radius,
              NEAR_BOUND - mMallet.radius)
      );

      float distance =
          Geometry.vectorBetween(mBlueMalletPosition, mPuckPosition).length();

      if(distance < (mPuck.radius + mMallet.radius)) {
        mPuckVector = Geometry.vectorBetween(
            mPrevBlueMalletPosition, mBlueMalletPosition
        );
      }
      mBlueMalletPosition = new Geometry.Point(
          clamp(touchedPoint.x,
              LEFT_BOUND + mMallet.radius,
              RIGHT_BOUND - mMallet.radius),
          mMallet.height / 2f,
          clamp(touchedPoint.z,
              0f + mMallet.radius,
              NEAR_BOUND - mMallet.radius));
    }
  }

  private Geometry.Ray convertNormalized2DPointToRay(float normalizedX, float normalizedY) {

    final float [] nearPointNdc = {normalizedX, normalizedY, -1, 1};
    final float [] farPointNdc = {normalizedX, normalizedY, 1, 1};

    final float [] nearPointWorld = new float[4];
    final float [] farPointWorld = new float[4];

    multiplyMV(nearPointWorld, 0,
        mInvertedViewProjectionMatrix, 0, nearPointNdc, 0);
    multiplyMV(farPointWorld, 0,
        mInvertedViewProjectionMatrix, 0, farPointNdc, 0);

    divideByW(nearPointWorld);
    divideByW(farPointWorld);

    Geometry.Point nearPointRay =
        new Geometry.Point(nearPointWorld[0],
            nearPointWorld[1], nearPointWorld[2]);
    Geometry.Point farPointRay =
        new Geometry.Point(farPointWorld[0],
            farPointWorld[1], farPointWorld[2]);

    return new Geometry.Ray(nearPointRay, Geometry.vectorBetween(nearPointRay, farPointRay));
  }

  private void divideByW(float[] vector) {
    vector[0] /= vector[3];
    vector[1] /= vector[3];
    vector[2] /= vector[3];
  }

  private float clamp(float value, float min, float max) {
    return Math.min(max, Math.max(value, min));
  }
}
