package es.noa.rad.game.engine.core;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import es.noa.rad.game.engine.configuration.settings.GameSettings;

  /**
   *
   */
  public final class GameTiming {

    /**
     *
     */
    public static final double FRAMERATE = 60.0D;

    /**
     *
     */
    public static final int MAXIMUM_UPDATES_PER_FRAME = 5;

    /**
     *
     */
    public static final float MAXIMUM_ACCUMULATED_TIME = 0.5F;

    /**
     *
     */
    private static final long NANOSECONDS_IN_SECOND
      = TimeUnit.SECONDS.toNanos(1L);

    /**
     *
     */
    private static GameTiming instance = null;

    /**
     *
     */
    private Consumer<Float> updateCallback;

    /**
     *
     */
    private Consumer<Float> renderCallback;

    /**
     *
     */
    private boolean running;

    /**
     *
     */
    private long previousTime;

    /**
     *
     */
    private float maxAccumulatedTime;

    /**
     *
     */
    private int maxUpdatesPerFrame;

    /**
     *
     */
    private double deltaTime;

    /**
     *
     */
    private double updateTime;

    /**
     *
     */
    private float fixedDeltaTime;

    /**
     *
     */
    private double maxDeltaTime;

    /**
     *
     */
    private int totalSkippedUpdates;

    /**
     *
     */
    private int ups;

    /**
     *
     */
    private long upsTime;

    /**
     *
     */
    private int fps;

    /**
     *
     */
    private long fpsTime;

    /**
     *
     */
    private GameTiming() {
      this.running = false;
      this.previousTime = System.nanoTime();
      this.deltaTime = 0D;
      this.totalSkippedUpdates = 0;

      /* Initialize metrics counters and timers. */
      this.resetUps();
      this.resetUpsTime();
      this.resetFps();
      this.resetFpsTime();
    }

    /**
     *
     */
    private static synchronized void createInstance() {
      /*
       * Synchronized creator to protect against possible multi-threading
       * problems.
       */
      if (GameTiming.instance == null) {
        GameTiming.instance = new GameTiming();
      }
    }

    /**
     *
     * @return {@code GameTiming}
     */
    public static GameTiming get() {
      if (GameTiming.instance == null) {
        GameTiming.createInstance();
      }
      return GameTiming.instance;
    }

    /**
     *
     */
    public void init() {

      /* Load configuration values once to avoid repeated lookups. */

      /* Updates per second (e.g., 60.0 = 60 UPS). */
      final double updatesPerSecond
        = GameSettings.GAME_UPDATES_PER_SECOND
          .get(GameTiming.FRAMERATE);

      /* Maximum time accumulation in seconds (e.g., 0.5 = 500ms). */
      this.maxAccumulatedTime
        = GameSettings.GAME_MAXIMUM_ACCUMULATED_TIME
          .get(GameTiming.MAXIMUM_ACCUMULATED_TIME);

      /* Maximum updates per frame (spiral of death protection). */
      this.maxUpdatesPerFrame
        = GameSettings.GAME_MAXIMUM_UPDATES_PER_FRAME
          .get(GameTiming.MAXIMUM_UPDATES_PER_FRAME);

      /* Establish the time that must elapse between each update. */
      this.updateTime
        = ((double) (GameTiming.NANOSECONDS_IN_SECOND / updatesPerSecond));

      /* Fixed timestep for deterministic updates. */
      this.fixedDeltaTime
        = ((float) (1.0D / updatesPerSecond));

      /* Maximum delta time threshold (in update units). */
      this.maxDeltaTime
        = ((double) (this.maxAccumulatedTime * updatesPerSecond));

      this.start();
    }

    /**
     *
     */
    public void start() {
      this.running = true;
    }

    /**
     *
     */
    public void stop() {
      this.running = false;
    }

    /**
     * Processes one frame of the game loop.
     * Handles timing accumulation, spiral of death protection,
     * fixed timestep updates, and rendering with interpolation.
     *
     * This method performs a complete frame tick:
     * 1. Accumulates elapsed time
     * 2. Applies spiral of death protection if needed
     * 3. Executes fixed timestep updates (up to maxUpdatesPerFrame)
     * 4. Renders with interpolation alpha
     * 5. Updates FPS/UPS metrics
     *
     * @return {@code boolean}
     */
    public boolean tick() {
      if (!this.running) {
        return false;
      }

      final long currentTime = System.nanoTime();

      /*
       * Accumulate elapsed time normalized to "update units".
       * deltaTime >= 1.0 means one update is needed.
       */
      this.deltaTime
        += ((currentTime - this.previousTime) / this.updateTime);
      this.previousTime = currentTime;

      /*
       * Protection against spiral of death:
       * If too much time accumulated, reset to maximum threshold.
       * This prevents infinite catch-up loop on very slow hardware.
       */
      if (this.deltaTime > this.maxDeltaTime) {
        System.out.printf(
          "Delta time too high (%.2f updates), "
          + "resetting to %.2f (max %.2f seconds).%n",
          this.deltaTime,
          this.maxDeltaTime,
          this.maxAccumulatedTime
        );
        final int skippedUpdates = ((int) (this.deltaTime - this.maxDeltaTime));
        this.totalSkippedUpdates += skippedUpdates;
        this.deltaTime = this.maxDeltaTime;
      }

      /*
       * Catch-up loop: Run accumulated updates with fixed timestep.
       * Limited to maxUpdatesPerFrame to prevent spiral of death.
       */
      int updateCount = 0;

      while ((this.deltaTime >= 1.0D)
          && (updateCount < this.maxUpdatesPerFrame)) {
        this.update(this.fixedDeltaTime);
        this.deltaTime--;
        updateCount++;
      }

      /* Discard remaining updates if limit was reached. */
      if (this.deltaTime >= 1.0D) {
        final int skipped = ((int) this.deltaTime);
        this.totalSkippedUpdates += skipped;
        this.deltaTime -= skipped;
        if (skipped > 0) {
          System.out.printf(
            "Skipped %d updates (limit: %d per frame) "
            + "to prevent spiral of death.%n",
            skipped,
            this.maxUpdatesPerFrame
          );
        }
      }

      /*
       * Render with interpolation alpha (0.0 to 1.0).
       * Alpha represents progress between current and next update,
       * allowing smooth visuals even with fixed physics timestep.
       */
      this.render((float) this.deltaTime);

      return true;
    }

    /**
     *
     * @param _deltaTime {@code float}
     */
    private void update(
        final float _deltaTime) {
      if (this.updateCallback != null) {
        this.updateCallback.accept(_deltaTime);
      } else {
        /* Default fallback if not configured the update callback. */
        Window.get().update(_deltaTime);
      }
      this.increaseUps();
      final long currentTime = System.currentTimeMillis();
      if (currentTime > this.upsTime) {
        System.out.printf(
          "Updates Per Second (UPS): %d, Total Skipped Updates: %d.%n",
          this.ups,
          this.totalSkippedUpdates
        );
        this.resetUpsTime();
        this.resetUps();
      }
    }

    /**
     *
     * @param _deltaTime {@code float}
     */
    private void render(
        final float _deltaTime) {
      if (this.renderCallback != null) {
        this.renderCallback.accept(_deltaTime);
      } else {
        /* Default fallback if not configured the render callback. */
        Window.get().render(_deltaTime);
      }
      this.increaseFps();
      final long currentTime = System.currentTimeMillis();
      if (currentTime > this.fpsTime) {
        System.out.printf(
          "Frames Per Second (FPS): %d.%n",
          this.fps
        );
        this.resetFpsTime();
        this.resetFps();
      }
    }

    /**
     *
     * @param _updateCallback {@code Consumer<Float>}
     */
    public void updateCallback(
        final Consumer<Float> _updateCallback) {
      this.updateCallback = _updateCallback;
    }

    /**
     *
     * @param _renderCallback {@code Consumer<Float>}
     */
    public void renderCallback(
        final Consumer<Float> _renderCallback) {
      this.renderCallback = _renderCallback;
    }

    /**
     *
     */
    private void increaseUps() {
      this.ups++;
    }

    /**
     *
     */
    private void resetUps() {
      this.ups = 0;
    }

    /**
     *
     */
    private void resetUpsTime() {
      this.upsTime
        = (System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(1L));
    }

    /**
     *
     */
    private void increaseFps() {
      this.fps++;
    }

    /**
     *
     */
    private void resetFps() {
      this.fps = 0;
    }

    /**
     *
     */
    private void resetFpsTime() {
      this.fpsTime
        = (System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(1L));
    }

  }
