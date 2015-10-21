package com.mobilechallenge.game.renderers;

import android.content.Context;
import android.opengl.GLSurfaceView;
import com.mobilechallenge.game.R;
import com.mobilechallenge.game.controllers.GameMechanics;
import com.mobilechallenge.game.objects.ChipObject;
import com.mobilechallenge.game.objects.EnemyObject;
import com.mobilechallenge.game.programs.DefaultTextureProgram;
import com.mobilechallenge.game.programs.SimpleSingleColorShaderProgram;
import com.mobilechallenge.game.programs.SimpleVaryingColorShaderProgram;
import com.mobilechallenge.game.ui.ChipView;
import com.mobilechallenge.game.ui.DeckView;
import com.mobilechallenge.game.ui.EnemyView;
import com.mobilechallenge.game.utils.Geometry;
import com.mobilechallenge.game.utils.TextureHelper;
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

  private static final int ANGRY = 0;
  private static final int VERY_ANGRY = 1;
  private static final int SAD = 2;
  private static final int SMILE = 3;

  // @formatter:off
    private final float[] mProjectionMatrix = new float[16];
   private final float[  ] mModelMatrix = new float[16];
  private final float[    ] mModelProjectionMatrix = new float[16];
           private int [] mTextures;
  // @formatter:on
  private final Context mContext;
  private SimpleVaryingColorShaderProgram mVaryingColorProgram;
  private SimpleSingleColorShaderProgram mSingleColorProgram;
  private DefaultTextureProgram mTextureProgram;

  private GameMechanics mGameMechanics;

  // views
  private DeckView mDeckView;
  private ChipView mChipView;
  private EnemyView mEnemyView;

  private int mTempTexture;

  private float mInterpolation = 0f;

  public GameRenderer(Context ctx, GameMechanics gameMechanics) {
    mContext = ctx;

    mGameMechanics = gameMechanics;
  }

  public void setInterpolation(float interpolation) {
    mInterpolation = interpolation;
  }

  @Override public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

    mVaryingColorProgram = new SimpleVaryingColorShaderProgram(mContext);
    mSingleColorProgram = new SimpleSingleColorShaderProgram(mContext);
    mTextureProgram = new DefaultTextureProgram(mContext);

    mDeckView = new DeckView(); // since it doesn't need aspect ratio, it's initialized here

    mTextures = TextureHelper.loadTextures(mContext, R.drawable.texture_angry_256_256,
        R.drawable.texture_very_angry_256_256, R.drawable.texture_sad_256_256,
        R.drawable.texture_smile_256_256);
  }

  @Override public void onSurfaceChanged(GL10 gl, int width, int height) {
    glViewport(0, 0, width, height);

    // currently it's width / height
    final float aspectRatio = (float) width / (float) height;

    Timber.i("Width is %d, height is %d, aspect is %f", width, height, aspectRatio);

    mChipView = new ChipView(32, aspectRatio);
    mEnemyView = new EnemyView(32, aspectRatio);

    mGameMechanics.createGameFromParams(aspectRatio);
    mGameMechanics.setChipView(mChipView).setEnemyView(mEnemyView);

    // use aspect ratio not here, but later
    orthoM(mProjectionMatrix, 0, -1f, 1f, -1f, 1f, -1f, 1f);
  }

  @Override public void onDrawFrame(GL10 gl) {
    glClear(GL_COLOR_BUFFER_BIT);

    mVaryingColorProgram.useProgram();
    mDeckView.bindData(mVaryingColorProgram);
    mVaryingColorProgram.setUniforms(mProjectionMatrix);
    mDeckView.draw();

    final ChipObject chip = mGameMechanics.getChipObject();
    final Geometry.Point chipPosition = chip.getInterpolatedPosition(mInterpolation);

    // todo(me), 10/20/15: add color gradient to faces:
    /**
     * if p <= 1.0
     * colorT = colorA * p + colorB * (1.0 - p);
     * else
     * colorT = colorB * (p - 1.0) + colorC * (2.0 - p);
     */
    mTextureProgram.useProgram();
    mChipView.bindData(mTextureProgram);
    positionObjectInScene(chipPosition.x, chipPosition.y);
    mTextureProgram.setUniforms(mModelProjectionMatrix, mTextures[SMILE], 0);
    chip.draw();

    final List<EnemyObject> enemies = mGameMechanics.getEnemyObjects();

    mEnemyView.bindData(mTextureProgram);
    for (int i = 0; i < enemies.size(); i++) {
      final EnemyObject enemy = enemies.get(i);
      final Geometry.Point enemyPosition = enemy.getInterpolatedPosition(mInterpolation);
      positionObjectInScene(enemyPosition.x, enemyPosition.y);
      mTextureProgram.setUniforms(mModelProjectionMatrix, mTextures[VERY_ANGRY], i);
      enemy.draw();
    }
  }

  private void positionObjectInScene(float x, float y) {
    setIdentityM(mModelMatrix, 0);
    translateM(mModelMatrix, 0, x, y, 0f);
    multiplyMM(mModelProjectionMatrix, 0, mProjectionMatrix, 0, mModelMatrix, 0);
  }
}
