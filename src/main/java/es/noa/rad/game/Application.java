package es.noa.rad.game;

import es.noa.rad.game.engine.config.Configuration;
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
          Thread.sleep(Configuration.get().getFrameTime());
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
      final Configuration config = Configuration.get();
      Window.get().init(
        config.getWindowWidth(),
        config.getWindowHeight(),
        config.getWindowTitle()
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
