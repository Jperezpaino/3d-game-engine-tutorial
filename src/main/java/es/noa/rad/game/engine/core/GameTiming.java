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
    private Runnable inputCallback;

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
     * Time of the last FPS metrics reset (in milliseconds).
     */
    private long fpsTime;

    /**
     * Indicates whether vertical synchronization (VSync) is enabled.
     * When true, frame rate is controlled by monitor refresh rate.
     * When false, FPS cap is applied based on maxFramesPerSecond.
     */
    private boolean vSyncEnabled;

    /**
     * Maximum frames per second when VSync is disabled.
     * Value of 0 means unlimited FPS (no cap applied).
     * Common values: 60, 120, 144, 240.
     */
    private int maxFramesPerSecond;

    /**
     * Target time per frame in nanoseconds for FPS limiting.
     * Calculated as NANOSECONDS_IN_SECOND / maxFramesPerSecond.
     * Value of 0.0 means no FPS cap (VSync enabled or unlimited FPS).
     */
    private double renderTime;

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
     * Initializes the game timing system with configuration values.
     *
     * <p>This method loads all timing-related settings including:
     * <ul>
     *   <li>VSync status and FPS cap configuration</li>
     *   <li>Updates per second (UPS) for fixed timestep</li>
     *   <li>Spiral of death protection parameters</li>
     * </ul>
     *
     * <p>FPS limiting behavior:
     * <ul>
     *   <li>If VSync is enabled: renderTime = 0 (no manual FPS cap)</li>
     *   <li>If VSync is disabled and maxFramesPerSecond > 0:
     *       renderTime calculated to limit FPS</li>
     *   <li>If maxFramesPerSecond = 0: renderTime = 0 (unlimited FPS)</li>
     * </ul>
     *
     * <p>Must be called before starting the game loop.
     */
    public void init() {

      /* Load configuration values once to avoid repeated lookups. */

      /* VSync enabled status. */
      this.vSyncEnabled
        = GameSettings.GAME_VERTICAL_SYNCHRONIZATION.get();

      /* Maximum FPS when VSync is disabled (0 = unlimited). */
      this.maxFramesPerSecond
        = GameSettings.GAME_MAXIMUM_FRAMES_PER_SECOND.get();

      /* Updates per second (e.g., 60.0 = 60 UPS). */
      final double updatesPerSecond
        = GameSettings.GAME_UPDATES_PER_SECOND.get();

      /* Calculate render time in nanoseconds for FPS cap. */
      if (!this.vSyncEnabled && this.maxFramesPerSecond > 0) {
        this.renderTime
          = ((double) (GameTiming.NANOSECONDS_IN_SECOND
              / this.maxFramesPerSecond));
      } else {
        this.renderTime = 0.0D;  /* No cap needed (VSync or unlimited). */
      }

      /* Maximum time accumulation in seconds (e.g., 0.5 = 500ms). */
      this.maxAccumulatedTime
        = GameSettings.GAME_MAXIMUM_ACCUMULATED_TIME.get();

      /* Maximum updates per frame (spiral of death protection). */
      this.maxUpdatesPerFrame
        = GameSettings.GAME_MAXIMUM_UPDATES_PER_FRAME.get();

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
     * Starts the game timing system.
     * Sets the running flag to true, allowing tick() to process frames.
     */
    public void start() {
      this.running = true;
    }

    /**
     * Stops the game timing system.
     * Sets the running flag to false, causing tick() to return false.
     */
    public void stop() {
      this.running = false;
    }

    /**
     * Processes one frame of the game loop.
     * Handles timing accumulation, spiral of death protection,
     * fixed timestep updates, rendering with interpolation, and FPS limiting.
     *
     * <p>This method performs a complete frame tick:
     * <ol>
     *   <li>Accumulates elapsed time</li>
     *   <li>Applies spiral of death protection if needed</li>
     *   <li>Processes input</li>
     *   <li>Executes fixed timestep updates (up to maxUpdatesPerFrame)</li>
     *   <li>Renders with interpolation alpha</li>
     *   <li>Updates FPS/UPS metrics</li>
     *   <li>Limits frame rate when VSync is disabled</li>
     * </ol>
     *
     * <p>FPS limiting (step 7):
     * <ul>
     *   <li>Only active when renderTime > 0
     *       (VSync disabled and FPS cap set)</li>
     *   <li>Calculates elapsed time for render and update</li>
     *   <li>Sleeps thread if frame completed faster than target</li>
     *   <li>Ensures consistent frame rate without VSync</li>
     * </ul>
     *
     * @return {@code true} if the game loop should continue,
     *         {@code false} if it should stop
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

      this.input();

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

      /* FPS cap: Only when VSync is disabled and FPS limit is configured. */
      if (this.renderTime > 0.0D) {
        /* Calculate the time it took to generate the render and update. */
        final long elapsedTime = System.nanoTime() - currentTime;

        /* Calculate how long we have to wait. */
        final long sleepTime = ((long) ((this.renderTime - elapsedTime)
            / TimeUnit.MILLISECONDS.toNanos(1L)));
        if (sleepTime > 0L) {
          try {
            Thread.sleep(sleepTime);
          } catch (final InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
          }
        }
      }

      return true;
    }

    /**
     *
     */
    private void input() {
      if (this.inputCallback != null) {
        this.inputCallback.run();
      } else {
        /* Default fallback if not configured the input callback. */
        Window.get().input();
      }
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
     * @param _inputCallback {@code Runnable}
     */
    public void inputCallback(
        final Runnable _inputCallback) {
      this.inputCallback = _inputCallback;
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
