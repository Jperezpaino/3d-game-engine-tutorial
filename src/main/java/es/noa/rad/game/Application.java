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
      this.resetUps();
      this.resetUpsTime();
      this.resetFps();
      this.resetFpsTime();
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

      /* Establish the time that must elapse between each update. */
      final double updateTime
        = ((double) (Application.NANOSECONDS_IN_SECOND
        / ((double) GameSettings.GAME_UPDATES_PER_SECOND
          .get(Application.FRAMERATE))));

      /* Fixed timestep for deterministic updates. */
      final float fixedDeltaTime
        = ((float) (1.0D / ((double) GameSettings.GAME_UPDATES_PER_SECOND
          .get(Application.FRAMERATE))));

      /* Maximum updates per frame (spiral of death protection). */
      final int maxUpdatesPerFrame
        = GameSettings.GAME_MAXIMUM_UPDATES_PER_FRAME
          .get(Application.MAXIMUM_UPDATES_PER_FRAME);

      /*
       * Main game loop: runs until the user closes the window.
       * VSync controls the frame rate automatically via swapBuffers().
       */
      while ((this.running)
          && (!Window.get().shouldClose())) {
        final long currentTime = System.nanoTime();

        /* Accumulate time in "update units". */
        this.deltaTime
          += ((currentTime - this.previousTime) / updateTime);
        this.previousTime = currentTime;

        /* Protection against spiral of death. */
        if (this.deltaTime
          > (((float) (GameSettings.GAME_MAXIMUM_ACCUMULATED_TIME
             .get(Application.MAXIMUM_ACCUMULATED_TIME)))
           * ((double) (GameSettings.GAME_UPDATES_PER_SECOND
             .get(Application.FRAMERATE))))) {
          System.out.printf("Game too far behind, resetting delta time.%n");
          final int skippedUpdates
            = ((int) (this.deltaTime
            - (((float) (GameSettings.GAME_MAXIMUM_ACCUMULATED_TIME
               .get(Application.MAXIMUM_ACCUMULATED_TIME)))
             * ((double) (GameSettings.GAME_UPDATES_PER_SECOND
               .get(Application.FRAMERATE))))));
            this.totalSkippedUpdates += skippedUpdates;
            this.deltaTime
              = (((float) (GameSettings.GAME_MAXIMUM_ACCUMULATED_TIME
                .get(Application.MAXIMUM_ACCUMULATED_TIME)))
               * ((double) (GameSettings.GAME_UPDATES_PER_SECOND
                .get(Application.FRAMERATE))));
        }

        /*
         * Run all accumulated updates with fixed timestep.
         * Limit updates per frame to prevent spiral of death.
         */
        int updateCount = 0;

        /* Run all accumulated updates with fixed timestep. */
        while ((this.deltaTime >= 1.0D)
            && (updateCount < maxUpdatesPerFrame)) {
          this.update(fixedDeltaTime);
          this.deltaTime--;
          updateCount++;
        }

        /* If there are still pending updates after the deadline. */
        if (this.deltaTime >= 1.0D) {
          final int skipped = ((int) this.deltaTime);
          this.totalSkippedUpdates += skipped;
          this.deltaTime -= skipped;
          if (skipped > 0) {
            System.out.printf(
              "Skipped %d updates to prevent spiral of death.%n",
              skipped
            );
          }
        }

        /* Render with interpolation alpha (0.0 to 1.0). */
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
