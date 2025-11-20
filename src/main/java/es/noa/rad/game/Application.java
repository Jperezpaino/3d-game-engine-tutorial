package es.noa.rad.game;

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
      while (true) {
        this.update();
        this.render();
      }
    }

    /**
     *
     */
    private void init() {
      System.out.println("Initializing Game!");
    }

    /**
     *
     */
    private void update() {
      System.out.println("Updating Game!");
    }

    /**
     *
     */
    private void render() {
      System.out.println("Rendering Game!");
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
