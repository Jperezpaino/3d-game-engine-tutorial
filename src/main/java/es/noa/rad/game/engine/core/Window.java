package es.noa.rad.game.engine.core;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryUtil;

import es.noa.rad.game.engine.event.KeyboardEventHandler;
import es.noa.rad.game.engine.event.MouseEventHandler;

  /**
   * Singleton manager for the GLFW window and OpenGL context.
   *
   * <p>This class handles window creation, configuration, and lifecycle:
   * <ul>
   *   <li>GLFW window creation and initialization</li>
   *   <li>OpenGL context setup</li>
   *   <li>Input callback registration</li>
   *   <li>VSync control</li>
   *   <li>Frame buffer swapping and event polling</li>
   * </ul>
   *
   * <p>Thread-safe singleton implementation with lazy initialization.
   * The window is centered on the primary monitor during initialization.
   *
   * <p>Usage example:
   * <pre>{@code
   * Window window = Window.get();
   * window.init(800, 600, "My Game");
   * window.enableVSync();
   *
   * while (!window.shouldClose()) {
   *     window.input();
   *     window.update(deltaTime);
   *     window.render(deltaTime);
   *     window.swapBuffers();
   * }
   *
   * window.close();
   * }</pre>
   *
   * @see KeyboardEventHandler
   * @see MouseEventHandler
   */
  public final class Window {

    /**
     * Singleton instance of the window manager.
     */
    private static Window instance = null;

    /**
     * GLFW window handle. MemoryUtil.NULL if not yet created.
     */
    private long glfwWindow;

    /**
     * Window width in screen coordinates (pixels).
     */
    private int width;

    /**
     * Window height in screen coordinates (pixels).
     */
    private int height;

    /**
     * Window title displayed in the title bar.
     */
    private String title;

    /**
     * Private constructor to enforce singleton pattern.
     * Initializes window properties to default values.
     */
    private Window() {
      this.width = 0;
      this.height = 0;
      this.title = "";
      this.glfwWindow = MemoryUtil.NULL;
    }

    /**
     * Creates the singleton instance in a thread-safe manner.
     * Uses synchronized method to prevent multiple instances
     * in multi-threaded environments.
     */
    private static synchronized void createInstance() {
      /*
       * Synchronized creator to protect against possible multi-threading
       * problems.
       */
      if (Window.instance == null) {
        Window.instance = new Window();
      }
    }

    /**
     * Gets the singleton instance of the window manager.
     * Creates the instance on first call (lazy initialization).
     *
     * @return the singleton {@code Window} instance
     */
    public static Window get() {
      if (Window.instance == null) {
        Window.createInstance();
      }
      return Window.instance;
    }

    /**
     * Initializes GLFW, creates the window, and sets up input callbacks.
     *
     * <p>This method performs the following steps:
     * <ol>
     *   <li>Initializes GLFW library</li>
     *   <li>Creates window with specified dimensions and title</li>
     *   <li>Centers window on primary monitor</li>
     *   <li>Makes OpenGL context current</li>
     *   <li>Registers keyboard, mouse, and scroll callbacks</li>
     *   <li>Makes window visible</li>
     * </ol>
     *
     * <p>Prints error messages to stderr if initialization fails.
     *
     * @param _width the window width in pixels
     * @param _height the window height in pixels
     * @param _title the window title
     */
    public void init(
        final int _width,
        final int _height,
        final String _title) {
      this.width = _width;
      this.height = _height;
      this.title = _title;

      /*
       * Initialize GLFW. Most GLFW functions will not work before doing this.
       */
      if (!GLFW.glfwInit()) {
        System.err.printf("ERROR: GLFW wasn't initializied.%n");
        return;
      }

      /* Create the window. */
      this.glfwWindow =
        GLFW.glfwCreateWindow(
          this.width,
          this.height,
          this.title,
          MemoryUtil.NULL,
          MemoryUtil.NULL
        );

      if (this.glfwWindow == MemoryUtil.NULL) {
        System.err.printf("ERROR: Window wasn't created.%n");
        return;
      }

      /* Get primary monitor video mode for centering calculation. */
      final GLFWVidMode videoMode
        = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());

      final int centerX
        = ((videoMode.width() - this.width) / 2);
      final int centerY
        = ((videoMode.height() - this.height) / 2);

      /* Make the window position in the center of the screen. */
      GLFW.glfwSetWindowPos(this.glfwWindow, centerX, centerY);

      /*
       * Sets the OpenGL context of the specified window as the current context
       * for the calling thread.
       */
      GLFW.glfwMakeContextCurrent(this.glfwWindow);

      /* Register all input callbacks from event handlers. */
      GLFW.glfwSetKeyCallback(
        this.glfwWindow,
        KeyboardEventHandler.get().getGlfwKeyCallback()
      );
      GLFW.glfwSetCursorPosCallback(
        this.glfwWindow,
        MouseEventHandler.get().getGlfwCursorPosCallback()
      );
      GLFW.glfwSetScrollCallback(
        this.glfwWindow,
        MouseEventHandler.get().getGlfwScrollCallback()
      );
      GLFW.glfwSetMouseButtonCallback(
        this.glfwWindow,
        MouseEventHandler.get().getGlfwMouseButtonCallback()
      );

      /* Make the window visible. */
      GLFW.glfwShowWindow(this.glfwWindow);
    }

    /**
     * Enables vertical synchronization (VSync).
     *
     * <p>When VSync is enabled, {@link #swapBuffers()} will block until
     * the next vertical refresh, limiting the frame rate to the monitor's
     * refresh rate (typically 60 Hz or 144 Hz).
     *
     * <p>Call this after {@link #init(int, int, String)} to enable VSync.
     */
    public void enableVSync() {
      GLFW.glfwSwapInterval(1);
    }

    /**
     * Swaps the front and back frame buffers and polls for window events.
     *
     * <p>This method performs two critical operations:
     * <ol>
     *   <li>Swaps buffers: Displays the rendered frame (back buffer
     *       becomes front)</li>
     *   <li>Polls events: Processes queued input events (keyboard, mouse,
     *       etc.)</li>
     * </ol>
     *
     * <p>With VSync enabled, this call blocks until the next vertical
     * refresh. Without VSync, it returns immediately, potentially causing
     * screen tearing.
     *
     * <p>Should be called once per frame after rendering.
     */
    public void swapBuffers() {
      /*
       * Swap the front and back buffers. With VSync enabled, this call will
       * block until the next vertical refresh, limiting the frame rate to
       * the monitor's refresh rate.
       */
      GLFW.glfwSwapBuffers(this.glfwWindow);

      /*
       * Poll for window events (keyboard, mouse, window close, etc.).
       * This processes events that have been queued since the last call.
       */
      GLFW.glfwPollEvents();
    }

    /**
     * Processes user input for the current frame.
     *
     * <p>Currently handles:
     * <ul>
     *   <li>ESC key: Requests window close</li>
     *   <li>Left mouse button: Prints cursor and scroll position</li>
     * </ul>
     *
     * <p>This is a placeholder implementation for testing input systems.
     * In a real game, this would be replaced with game-specific input logic.
     */
    public void input() {
      /* Close window when ESC key is pressed. */
      if (KeyboardEventHandler.get().isKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
        GLFW.glfwSetWindowShouldClose(this.glfwWindow, true);
      }
      /* Debug: Print mouse state when left button is pressed. */
      if (MouseEventHandler.get()
        .isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
        System.out.printf(
          "(x: %.0f, y: %.0f, Scroll x: %.0f, Scroll y: %.0f)%n",
          MouseEventHandler.get().getCursorPositionX(),
          MouseEventHandler.get().getCursorPositionY(),
          MouseEventHandler.get().getCursorScrollX(),
          MouseEventHandler.get().getCursorScrollY()
        );
      }
    }

    /**
     * Updates the game state for the current frame.
     *
     * <p>This method is called once per frame with the time elapsed since
     * the last update. Typically used for game logic, physics, AI, etc.
     *
     * <p>Currently a placeholder implementation for future game logic.
     *
     * @param _deltaTime time elapsed since last update in seconds
     */
    public void update(
        final float _deltaTime) {
      // System.out.printf(
      //  "Updating Game! Delta: %.4f.%n",
      //  _deltaTime
      // );
    }

    /**
     * Renders the current frame.
     *
     * <p>This method is called once per frame with the interpolation factor
     * for the render. Typically used for drawing graphics, UI, etc.
     *
     * <p>Currently a placeholder implementation for future rendering logic.
     *
     * @param _deltaTime interpolation factor for smooth rendering
     */
    public void render(
        final float _deltaTime) {
      // System.out.printf(
      //   "Rendering Game! Delta: %.4f.%n",
      //   _deltaTime
      // );
    }

    /**
     * Checks if the window should close.
     *
     * <p>Returns true if the user has requested to close the window
     * (e.g., clicked the close button or pressed ESC).
     *
     * @return {@code true} if the window should close, {@code false} otherwise
     */
    public boolean shouldClose() {
      /* Check if the user has attempted to close the window. */
      return GLFW.glfwWindowShouldClose(this.glfwWindow);
    }

    /**
     * Cleans up all window resources and terminates GLFW.
     *
     * <p>This method performs cleanup in the following order:
     * <ol>
     *   <li>Frees keyboard event handler resources</li>
     *   <li>Frees mouse event handler resources</li>
     *   <li>Destroys the GLFW window</li>
     *   <li>Terminates GLFW library</li>
     * </ol>
     *
     * <p>Should be called when the application exits to prevent memory leaks.
     */
    public void close() {
      /* Free the Keyboard callback. */
      KeyboardEventHandler.get().close();

      /* Free the Mouse callback. */
      MouseEventHandler.get().close();

      /* Free the window callbacks and destroy the window. */
      GLFW.glfwDestroyWindow(this.glfwWindow);

      /* Terminate GLFW and free the error callback. */
      GLFW.glfwTerminate();
    }

    /**
     * Gets the window width in pixels.
     *
     * @return the window width
     */
    public int width() {
      return this.width;
    }

    /**
     * Gets the window height in pixels.
     *
     * @return the window height
     */
    public int height() {
      return this.height;
    }

    /**
     * Gets the window title.
     *
     * @return the window title
     */
    public String title() {
      return this.title;
    }

  }
