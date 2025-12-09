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
    private static final long NANOSECONDS_IN_SECOND
      = TimeUnit.SECONDS.toNanos(1L);

    /**
     *
     */
    private final Thread game;

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
      this.previousTime = System.nanoTime();
      this.deltaTime = 0D;
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
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public void run() {
      this.init();

      /* Establish the time that must elapse between each of the frames. */
      final double renderTime
        = (Application.NANOSECONDS_IN_SECOND
        / GameSettings.GAME_FRAMES_PER_SECOND.get(Application.FRAMERATE));

      /* Establish the time that must elapse between each update. */
      final double updateTime
        = (Application.NANOSECONDS_IN_SECOND
        / GameSettings.GAME_UPDATES_PER_SECOND.get(Application.FRAMERATE));

      /* Fixed timestep for deterministic updates. */
      final float fixedDeltaTime
        = (float) (1.0D / GameSettings.GAME_UPDATES_PER_SECOND
          .get(Application.FRAMERATE));

      /* Maximum updates per frame (spiral of death protection). */
      final int maxUpdatesPerFrame
        = GameSettings.GAME_MAXIMUM_UPDATES_PER_FRAME.get(5);

      /*
       * Run the rendering and updating loop until the user has attempted to
       * close the window.
       */
      while (!Window.get().shouldClose()) {
        final long currentTime = System.nanoTime();

        /* Accumulate time in "update units". */
        this.deltaTime
          += ((currentTime - this.previousTime) / updateTime);
        this.previousTime = currentTime;

        /*
         * Run all accumulated updates with fixed timestep.
         * Limit updates per frame to prevent spiral of death.
         */
        int updateCount = 0;
        while ((this.deltaTime >= 1.0D)
            && (updateCount < maxUpdatesPerFrame)) {
          this.update(fixedDeltaTime);
          this.deltaTime--;
          updateCount++;
        }

        /* Render with interpolation alpha (0.0 to 1.0). */
        this.render((float) this.deltaTime);

        /* Calculate the time it took to generate the render and update. */
        final long elapsedTime = System.nanoTime() - currentTime;

        /* Calculate how long we have to wait. */
        final long sleepTime
          = ((long) ((renderTime - elapsedTime)
          / TimeUnit.MILLISECONDS.toNanos(1L)));
        if (sleepTime > 0L) {
          try {
            Thread.sleep(sleepTime);
          } catch (
              final InterruptedException interruptedException) {
            interruptedException.printStackTrace();
          }
        }
      }

      this.close();
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
          "Updates Per Second (UPS): %d.%n",
          this.ups
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
