package es.noa.rad.game;

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
    private final Thread game;

    /**
     *
     */
    private Application() {
      super();
      this.game = new Thread(this, "Game");
      this.game.start();
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public void run() {
      this.init();

      /*
       * Run the rendering and updating loop until the user has attempted to
       * close the window.
       */
      while (!Window.get().shouldClose()) {
        this.update();
        this.render();
        try {
          Thread.sleep(
            (long) GameSettings.GAME_FREQUENCY_TIME.get()
          );
        } catch (
            final InterruptedException interruptedException) {
          interruptedException.printStackTrace();
        }
      }

      this.cleanup();
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
    }

    /**
     *
     */
    private void update() {
      Window.get().update();
    }

    /**
     *
     */
    private void render() {
      Window.get().render();
    }

    /**
     *
     */
    private void cleanup() {
      Window.get().cleanup();
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
