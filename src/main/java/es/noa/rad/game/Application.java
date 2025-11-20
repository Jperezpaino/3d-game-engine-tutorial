package es.noa.rad.game;

import es.noa.rad.game.engine.core.Window;

  /**
   *
   */
  public final class Application
      implements Runnable {

    /**
     *
     */
    private static final long FRAME_TIME = 16L; /* Approximately 60 FPS. */

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
    public static final String TITLE = "3D Game Engine Tutorial";

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
          Thread.sleep(Application.FRAME_TIME);
        } catch (
            final InterruptedException interruptedException) {
          interruptedException.printStackTrace();
        }
      }
    }

    /**
     *
     */
    private void init() {
      Window.get().init(
        Application.WIDTH,
        Application.HEIGHT,
        Application.TITLE
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
     * @param _arguments {@code String...}
     */
    public static void main(
        final String... _arguments) {
      new Application();
    }

  }
