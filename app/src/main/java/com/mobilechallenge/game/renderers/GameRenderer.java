package com.mobilechallenge.game.renderers;

import android.content.Context;
import android.opengl.GLSurfaceView;
import com.mobilechallenge.game.controllers.GameState;
import com.mobilechallenge.game.objects.ChipObject;
import com.mobilechallenge.game.objects.EnemyObject;
import com.mobilechallenge.game.programs.SimpleSingleColorShaderProgram;
import com.mobilechallenge.game.programs.SimpleVaryingColorShaderProgram;
import com.mobilechallenge.game.ui.ChipView;
import com.mobilechallenge.game.ui.DeckView;
import com.mobilechallenge.game.ui.EnemyView;
import java.util.List;
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
public class GameRenderer implements GLSurfaceView.Renderer {

  // @formatter:off
    private final float[] mProjectionMatrix = new float[16];
   private final float[  ] mModelMatrix = new float[16];
  private final float[    ] mModelProjectionMatrix = new float[16];
  // @formatter:on
  private final Context mContext;
  private SimpleVaryingColorShaderProgram mVaryingColorProgram;
  private SimpleSingleColorShaderProgram mSingleColorProgram;

  private GameState mGameState;

  // views
  private DeckView mDeckView;
  private ChipView mChipView;
  private EnemyView mEnemyView;

  private float mInterpolation;

  public GameRenderer(Context ctx, GameState gameState) {
    mContext = ctx;

    mGameState = gameState;
  }

  public void setInterpolation(float interpolation) {
    mInterpolation = interpolation;
  }

  @Override public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

    mVaryingColorProgram = new SimpleVaryingColorShaderProgram(mContext);
    mSingleColorProgram = new SimpleSingleColorShaderProgram(mContext);
    mDeckView = new DeckView(); // since it doesn't need aspect ratio, it's initialized here
  }

  @Override public void onSurfaceChanged(GL10 gl, int width, int height) {
    glViewport(0, 0, width, height);

    // currently it's width / height
    final float aspectRatio = (float) width / (float) height;

    Timber.i("Width is %d, height is %d, aspect is %f", width, height, aspectRatio);

    mChipView = new ChipView(32, aspectRatio);
    mEnemyView = new EnemyView(32, aspectRatio);

    mGameState.initGame(aspectRatio);
    mGameState.setChipView(mChipView).setEnemyView(mEnemyView);

    // use aspect ratio not here, but later
    orthoM(mProjectionMatrix, 0, -1f, 1f, -1f, 1f, -1f, 1f);
  }

  @Override public void onDrawFrame(GL10 gl) {
    glClear(GL_COLOR_BUFFER_BIT);

    mVaryingColorProgram.useProgram();
    mDeckView.bindData(mVaryingColorProgram);
    mVaryingColorProgram.setUniforms(mProjectionMatrix);
    mDeckView.draw();

    final ChipObject chip = mGameState.getChipObject();

    mSingleColorProgram.useProgram();
    mChipView.bindData(mSingleColorProgram);
    positionObjectInScene(chip.getPosition().x, chip.getPosition().y);
    mSingleColorProgram.setUniforms(mModelProjectionMatrix, 1f, 0f, 0f);
    chip.draw();

    final List<EnemyObject> enemies = mGameState.getEnemyObjects();

    mEnemyView.bindData(mSingleColorProgram);
    for (EnemyObject enemy : enemies) {
      positionObjectInScene(enemy.getPosition().x, enemy.getPosition().y);
      mSingleColorProgram.setUniforms(mModelProjectionMatrix, 0f, 0f, 1f);
      enemy.draw();
    }
  }

  private void positionObjectInScene(float x, float y) {
    setIdentityM(mModelMatrix, 0);
    translateM(mModelMatrix, 0, x, y, 0f);
    multiplyMM(mModelProjectionMatrix, 0, mProjectionMatrix, 0, mModelMatrix, 0);
  }
}
