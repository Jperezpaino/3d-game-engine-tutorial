package es.noa.rad.game;

import es.noa.rad.game.engine.configuration.Configuration;
import es.noa.rad.game.engine.configuration.settings.GameSettings;
import es.noa.rad.game.engine.configuration.settings.WindowSettings;
import es.noa.rad.game.engine.core.GameTiming;
import es.noa.rad.game.engine.core.Window;

  /**
   * Main application class that orchestrates the game engine.
   *
   * <p>This class is responsible for:
   * <ul>
   *   <li>Creating and managing the game thread</li>
   *   <li>Initializing configuration, window, and game timing</li>
   *   <li>Running the main game loop</li>
   *   <li>Coordinating cleanup on shutdown</li>
   * </ul>
   *
   * <p>The application follows this lifecycle:
   * <ol>
   *   <li>Constructor creates game thread and starts it</li>
   *   <li>{@link #run()} initializes all subsystems</li>
   *   <li>Main loop executes until window closes</li>
   *   <li>Cleanup and shutdown</li>
   * </ol>
   *
   * <p>Thread model: The game runs on a dedicated thread separate from
   * the main thread. This allows for clean separation of initialization
   * and game loop execution.
   *
   * @see GameTiming
   * @see Window
   * @see Configuration
   */
  public final class Application
      implements Runnable {

    /**
     * Dedicated thread for running the game loop.
     */
    private final Thread game;

    /**
     * Flag indicating if the game loop should continue running.
     * Set to false to gracefully stop the game.
     */
    private boolean running;

    /**
     * Private constructor that creates and starts the game thread.
     *
     * <p>Initializes the game thread with name "Game" and starts it
     * immediately. The actual game initialization happens in
     * {@link #run()}.
     */
    private Application() {
      super();

      /* Create and start the game thread. */
      this.game = new Thread(this, "Game");
      this.running = false;
      this.game.start();
    }

    /**
     * Initializes all game subsystems and prepares for the main loop.
     *
     * <p>Initialization sequence:
     * <ol>
     *   <li>Load configuration from properties file</li>
     *   <li>Create window with configured dimensions and title</li>
     *   <li>Enable VSync if configured</li>
     *   <li>Register game timing callbacks (input, update, render)</li>
     *   <li>Initialize game timing system</li>
     *   <li>Start the game loop</li>
     * </ol>
     *
     * <p>Prints window creation details to stdout for verification.
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

      /* Initialize game timing (calculates frame/update rates). */
      GameTiming.get().init();

      /* Start the game loop. */
      this.start();
    }

    /**
     * Starts the game loop by setting the running flag to true.
     * Called automatically after initialization.
     */
    private void start() {
      this.running = true;
    }

    /**
     * Stops the game loop and game timing system.
     *
     * <p>Sets the running flag to false and stops the timing system.
     * This causes the main loop to exit gracefully.
     */
    private void stop() {
      this.running = false;
      GameTiming.get().stop();
    }

    /**
     * Main game loop execution method (Runnable interface).
     *
     * <p>This method runs on the game thread and executes:
     * <ol>
     *   <li>Initialization of all subsystems</li>
     *   <li>Main loop that continues while:
     *     <ul>
     *       <li>running flag is true</li>
     *       <li>window is not closed</li>
     *       <li>game timing tick succeeds</li>
     *     </ul>
     *   </li>
     *   <li>Cleanup and shutdown</li>
     * </ol>
     *
     * <p>The game timing system ({@link GameTiming#tick()}) handles
     * calling input, update, and render callbacks at appropriate rates.
     * This method only swaps buffers and polls events.
     *
     * @see GameTiming#tick()
     */
    @Override
    public void run() {
      this.init();

      /*
       * Main game loop: runs until the user closes the window.
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
     * Cleans up all resources before shutdown.
     * Closes the window and releases GLFW resources.
     */
    private void close() {
      Window.get().close();
    }

    /**
     * Application entry point.
     *
     * <p>Creates a new Application instance which automatically:
     * <ol>
     *   <li>Creates a game thread</li>
     *   <li>Starts the thread (triggering {@link #run()})</li>
     *   <li>Runs the game until exit</li>
     * </ol>
     *
     * @param _arguments command line arguments (currently unused)
     */
    public static void main(
        final String... _arguments) {
      new Application();
    }

  }
