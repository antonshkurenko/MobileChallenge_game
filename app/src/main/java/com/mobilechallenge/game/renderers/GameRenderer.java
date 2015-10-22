package com.mobilechallenge.game.renderers;

import android.content.Context;
import android.opengl.GLSurfaceView;
import com.mobilechallenge.game.R;
import com.mobilechallenge.game.controllers.GameMechanics;
import com.mobilechallenge.game.objects.ChipObject;
import com.mobilechallenge.game.objects.EnemyObject;
import com.mobilechallenge.game.programs.DefaultTextureProgram;
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

import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glEnable;
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
  private static final int SCARED = 4;
  private static final int LOLLIPOP = 5;

  // @formatter:off
        private final float[] mProjectionMatrix = new float[16];
       private final float[  ] mModelMatrix = new float[16];
      private final float[    ] mModelProjectionMatrix = new float[16];
            private int [      ] mTextures;
   private final float [        ] mYellow = new float[] { 0.901960784f, 0.91372549f, 0f };
  private final float [          ] mOrange = new float[] { 0.996078431f, 0.301960784f, 0.066666667f };
       private final float [] mRed = new float[] { 1f, 0.066666667f, 0f };
       private final float [] mGreen = new float[] { 0.101960784f, 0.580392157f, 0.192156863f };
       private final float [] mBlue = new float[] { 0f, 0f, 0.690196078f };
       private final float [] mWhite = new float[] { 1f, 1f, 1f };
  // @formatter:on
  private final Context mContext;

  private SimpleVaryingColorShaderProgram mVaryingColorProgram;
  private DefaultTextureProgram mTextureProgram;

  private GameMechanics mGameMechanics;

  // views
  private DeckView mDeckView;
  private ChipView mChipView;
  private EnemyView mEnemyView;

  private float mInterpolation = 0f;

  public GameRenderer(Context ctx) {
    mContext = ctx;
  }

  public void setGameMechanics(GameMechanics mechanics) {
    if (!mechanics.isInited()) {
      throw new IllegalStateException("Setting views to not initialized mechanics.");
    }
    mGameMechanics = mechanics.setChipView(mChipView).setEnemyView(mEnemyView);
  }

  public void setInterpolation(float interpolation) {
    mInterpolation = interpolation;
  }

  @Override public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

    mVaryingColorProgram = new SimpleVaryingColorShaderProgram(mContext);
    mTextureProgram = new DefaultTextureProgram(mContext);

    mDeckView = new DeckView(); // since it doesn't need aspect ratio, it's initialized here

    mTextures = TextureHelper.loadTextures(mContext, R.drawable.texture_angry_256_256,
        R.drawable.texture_very_angry_256_256, R.drawable.texture_sad_256_256,
        R.drawable.texture_smile_256_256, R.drawable.texture_scared_256_256,
        R.drawable.texture_test_256_256);
  }

  @Override public void onSurfaceChanged(GL10 gl, int width, int height) {
    glViewport(0, 0, width, height);

    // currently it's width / height
    final float aspectRatio = (float) width / (float) height;

    Timber.i("Width is %d, height is %d, aspect is %f", width, height, aspectRatio);

    mChipView = new ChipView(32, aspectRatio);
    mEnemyView = new EnemyView(32, aspectRatio);
    mGameMechanics.setAspectRatio(aspectRatio).setChipView(mChipView).setEnemyView(mEnemyView);

    // use aspect ratio not here, but later
    orthoM(mProjectionMatrix, 0, -1f, 1f, -1f, 1f, -1f, 1f);
  }

  @Override public void onDrawFrame(GL10 gl) {
    glClear(GL_COLOR_BUFFER_BIT);
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    /**
     * Drawing table
     */

    mVaryingColorProgram.useProgram();
    mDeckView.bindData(mVaryingColorProgram);
    mVaryingColorProgram.setUniforms(mProjectionMatrix);
    mDeckView.draw();

    /**
     * Start drawing textures
     */
    mTextureProgram.useProgram();

    final ChipObject chip = mGameMechanics.getChipObject();
    final List<EnemyObject> enemies = mGameMechanics.getEnemyObjects();

    /**
     * Drawing chip
     */

    if (chip != null) {
      final Geometry.Point chipPosition = chip.getInterpolatedPosition(mInterpolation);

      mChipView.bindData(mTextureProgram);
      positionObjectInScene(chipPosition.x, chipPosition.y);

      final int texture;
      final float[] color;
      if(mGameMechanics.isLollipop()) {
        texture = LOLLIPOP;
        color = mWhite;
      } else {

        if (enemies != null) {
          float minLength = Float.POSITIVE_INFINITY;
          color = new float[3];

          // check if chip is close to enemies
          for (EnemyObject enemy : enemies) {
            minLength = Math.min(minLength, chipPosition.distanceTo(enemy.getPosition()));
          }

          final float k = minLength / 1f;
          for (int j = 0; j < 3; j++) {
            color[j] = 0.5f * ((1f - k) * mBlue[j] + k * mGreen[j]) + 0.5f * mGreen[j];
          }

          if(k < 0.33f) {
            texture = SCARED;
          } else {
            texture = SMILE;
          }

        } else {
          color = mGreen;
          texture = SMILE;
        }
      }
      mTextureProgram.setUniforms(mModelProjectionMatrix, color, mTextures[texture], 0);
      chip.draw();
    }

    /**
     * Drawing enemies
     */

    if (enemies != null) {
      mEnemyView.bindData(mTextureProgram);
      final float startSpeed = mGameMechanics.getStartEnemyVector().length();
      final float maxSpeed = mGameMechanics.getMaxSpeed();
      for (int i = 0; i < enemies.size(); i++) {
        final EnemyObject enemy = enemies.get(i);
        final Geometry.Point enemyPosition = enemy.getInterpolatedPosition(mInterpolation);
        positionObjectInScene(enemyPosition.x, enemyPosition.y);

        int texture;

        final float speed = enemy.getSpeed().length();

        // percentage of current speed / max speed
        final float k = (speed - startSpeed) / (maxSpeed - startSpeed);

        if (k >= 0.5f) {
          texture = VERY_ANGRY;
        } else if (k >= 0.25f) {
          texture = ANGRY;
        } else {
          texture = SAD;
        }

        final float[] color = new float[3];

        if (k < 0.5) {
          for (int j = 0; j < 3; j++) {
            color[j] = 2 * ((0.5f - k) * mYellow[j] + k * mOrange[j]);
          }
        } else {
          for (int j = 0; j < 3; j++) {
            color[j] = 2 * ((1 - k) * mOrange[j] + (k - 0.5f) * mRed[j]);
          }
        }

        mTextureProgram.setUniforms(mModelProjectionMatrix, color, mTextures[texture], i);
        enemy.draw();
      }
    }
  }

  private void positionObjectInScene(float x, float y) {
    setIdentityM(mModelMatrix, 0);
    translateM(mModelMatrix, 0, x, y, 0f);
    multiplyMM(mModelProjectionMatrix, 0, mProjectionMatrix, 0, mModelMatrix, 0);
  }
}
