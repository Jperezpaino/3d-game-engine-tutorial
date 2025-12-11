package es.noa.rad.game;

import java.util.concurrent.TimeUnit;
import es.noa.rad.game.engine.core.Window;
import es.noa.rad.game.engine.configuration.Configuration;
import es.noa.rad.game.engine.configuration.settings.GameSettings;
import es.noa.rad.game.engine.configuration.settings.WindowSettings;

  /**
   *
   */
  public final class Application
      implements Runnable {

    /**
     *
     */
    public static final int WIDTH = 1280;

    /**
     *
     */
    public static final int HEIGHT = 720;

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
    private final Thread game;

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
    private double deltaTime;

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
    private Application() {
      super();

      /* Initialize metrics counters and timers. */
      this.resetUps();
      this.resetUpsTime();
      this.resetFps();
      this.resetFpsTime();

      /* Create and start the game thread. */
      this.game = new Thread(this, "Game");
      this.running = false;
      this.previousTime = System.nanoTime();
      this.deltaTime = 0D;
      this.totalSkippedUpdates = 0;
      this.game.start();
    }

    /**
     *
     */
    private void init() {
      /* Initialize configuration and create window. */
      Configuration.get().init();
      Window.get().init(
        WindowSettings.WINDOW_WIDTH.get(Application.WIDTH),
        WindowSettings.WINDOW_HEIGHT.get(Application.HEIGHT),
        WindowSettings.WINDOW_TITLE.get()
      );

      System.out.printf(
        "Window created with a size (%dx%d) and with the title '%s'.%n",
        Window.get().width(),
        Window.get().height(),
        Window.get().title()
      );

      /* Enable VSync if configured. */
      if (((boolean) GameSettings.GAME_VERTICAL_SYNCHRONIZATION.get(true))) {
        Window.get().enableVSync();
      }

      /* Start the game loop. */
      this.start();
    }

    /**
     *
     */
    private void start() {
      this.running = true;
    }

    /**
     *
     */
    private void stop() {
      this.running = false;
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public void run() {
      this.init();

      /* Load configuration values once to avoid repeated lookups. */

      final double updatesPerSecond
        = GameSettings.GAME_UPDATES_PER_SECOND
          .get(Application.FRAMERATE);

      final float maxAccumulatedTime
        = GameSettings.GAME_MAXIMUM_ACCUMULATED_TIME
          .get(Application.MAXIMUM_ACCUMULATED_TIME);

      /* Maximum updates per frame (spiral of death protection). */
      final int maxUpdatesPerFrame
        = GameSettings.GAME_MAXIMUM_UPDATES_PER_FRAME
          .get(Application.MAXIMUM_UPDATES_PER_FRAME);

      /* Establish the time that must elapse between each update. */
      final double updateTime
        = ((double) (Application.NANOSECONDS_IN_SECOND / updatesPerSecond));

      /* Fixed timestep for deterministic updates. */
      final float fixedDeltaTime
        = ((float) (1.0D / updatesPerSecond));

      /* Maximum delta time threshold (in update units). */
      final double maxDeltaTime
        = ((double) (maxAccumulatedTime * updatesPerSecond));

      /*
       * Main game loop: runs until the user closes the window.
       * VSync controls the frame rate automatically via swapBuffers().
       */
      while ((this.running)
          && (!Window.get().shouldClose())) {
        final long currentTime = System.nanoTime();

        /*
         * Accumulate elapsed time normalized to "update units".
         * deltaTime >= 1.0 means one update is needed.
         */
        this.deltaTime
          += ((currentTime - this.previousTime) / updateTime);
        this.previousTime = currentTime;

        /*
         * Protection against spiral of death:
         * If too much time accumulated, reset to maximum threshold.
         * This prevents infinite catch-up loop on very slow hardware.
         */
        if (this.deltaTime > maxDeltaTime) {
          System.out.printf(
            "Delta time too high (%.2f updates), "
            + "resetting to %.2f (max %.2f seconds).%n",
            this.deltaTime,
            maxDeltaTime,
            maxAccumulatedTime
          );
          final int skippedUpdates = ((int) (this.deltaTime - maxDeltaTime));
          this.totalSkippedUpdates += skippedUpdates;
          this.deltaTime = maxDeltaTime;
        }

        /*
         * Catch-up loop: Run accumulated updates with fixed timestep.
         * Limited to maxUpdatesPerFrame to prevent spiral of death.
         */
        int updateCount = 0;

        while ((this.deltaTime >= 1.0D)
            && (updateCount < maxUpdatesPerFrame)) {
          this.update(fixedDeltaTime);
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
              maxUpdatesPerFrame
            );
          }
        }

        /*
         * Render with interpolation alpha (0.0 to 1.0).
         * Alpha represents progress between current and next update,
         * allowing smooth visuals even with fixed physics timestep.
         */
        this.render((float) this.deltaTime);

        /*
         * Swap buffers and poll events.
         * VSync makes this call block until the next vertical refresh.
         */
        Window.get().swapBuffers();
      }

      this.close();
      this.stop();
    }

    /**
     *
     * @param _deltaTime {@code float}
     */
    private void update(
        final float _deltaTime) {
      Window.get().update(_deltaTime);
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
      Window.get().render(_deltaTime);
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
     */
    private void close() {
      Window.get().close();
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

    /**
     *
     * @param _arguments {@code String...}
     */
    public static void main(
        final String... _arguments) {
      new Application();
    }

  }
