package es.noa.rad.game;

import es.noa.rad.game.engine.configuration.Configuration;
import es.noa.rad.game.engine.configuration.settings.GameSettings;
import es.noa.rad.game.engine.configuration.settings.WindowSettings;
import es.noa.rad.game.engine.core.GameTiming;
import es.noa.rad.game.engine.core.Window;

  /**
   *
   */
  public final class Application
      implements Runnable {

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
    private Application() {
      super();

      /* Create and start the game thread. */
      this.game = new Thread(this, "Game");
      this.running = false;
      this.game.start();
    }

    /**
     *
     */
    private void init() {
      /* Initialize configuration and create window. */
      Configuration.get().init();
      Window.get().init(
        WindowSettings.WINDOW_WIDTH.get(),
        WindowSettings.WINDOW_HEIGHT.get(),
        WindowSettings.WINDOW_TITLE.get()
      );

      System.out.printf(
        "Window created with a size (%dx%d) and with the title '%s'.%n",
        Window.get().width(),
        Window.get().height(),
        Window.get().title()
      );

      /* Enable VSync if configured. */
      if (((boolean) GameSettings.GAME_VERTICAL_SYNCHRONIZATION.get())) {
        Window.get().enableVSync();
      }

      /* Set up game timing callbacks. */
      GameTiming.get().updateCallback(
        (deltaTime) -> Window.get().update(deltaTime)
      );
      GameTiming.get().renderCallback(
        (deltaTime) -> Window.get().render(deltaTime)
      );
      GameTiming.get().inputCallback(
        () -> Window.get().input()
      );

      GameTiming.get().init();

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
      GameTiming.get().stop();
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public void run() {
      this.init();

      /*
       * Main game loop: runs until the user closes the window.
       * VSync controls the frame rate automatically via swapBuffers().
       */
      while ((this.running)
          && (!Window.get().shouldClose())
          && (GameTiming.get().tick())) {

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
     */
    private void close() {
      Window.get().close();
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
