package es.noa.rad.game.engine.event;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;

import es.noa.rad.game.engine.event.callback.KeyCallback;

  /**
   * Singleton handler for keyboard input events.
   *
   * <p>This class manages the state of all keyboard keys and provides
   * methods to query whether specific keys are currently pressed.
   *
   * <p>Thread-safe singleton implementation with lazy initialization.
   * The keyboard state is updated automatically by {@link KeyCallback}
   * which is registered with GLFW.
   *
   * <p>Usage example:
   * <pre>{@code
   * if (KeyboardEventHandler.get().isKeyPressed(GLFW.GLFW_KEY_W)) {
   *     // Move forward
   * }
   * }</pre>
   *
   * @see KeyCallback
   */
  public final class KeyboardEventHandler {

    /**
     * Singleton instance of the keyboard event handler.
     */
    private static KeyboardEventHandler instance = null;

    /**
     * GLFW keyboard callback that processes key events.
     */
    private final GLFWKeyCallback glfwKeyCallback;

    /**
     * Array storing the pressed state of all keyboard keys.
     * Index corresponds to GLFW key codes (GLFW_KEY_*).
     * Size is GLFW_KEY_LAST to cover all possible keys.
     */
    private final boolean[] keyPressed;

    /**
     * Private constructor to enforce singleton pattern.
     * Initializes the key state array and creates the GLFW callback.
     */
    private KeyboardEventHandler() {
      this.keyPressed = new boolean[GLFW.GLFW_KEY_LAST];
      this.glfwKeyCallback = new KeyCallback();
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
      if (KeyboardEventHandler.instance == null) {
        KeyboardEventHandler.instance = new KeyboardEventHandler();
      }
    }

    /**
     * Gets the singleton instance of the keyboard event handler.
     * Creates the instance on first call (lazy initialization).
     *
     * @return the singleton {@code KeyboardEventHandler} instance
     */
    public static KeyboardEventHandler get() {
      if (KeyboardEventHandler.instance == null) {
        KeyboardEventHandler.createInstance();
      }
      return KeyboardEventHandler.instance;
    }

    /**
     * Releases GLFW callback resources.
     * Should be called when shutting down the application
     * to prevent memory leaks.
     */
    public void close() {
      this.glfwKeyCallback.free();
    }

    /**
     * Gets the GLFW keyboard callback for registration with GLFW.
     * This callback should be registered with glfwSetKeyCallback().
     *
     * @return the {@code GLFWKeyCallback} instance
     */
    public GLFWKeyCallback getGlfwKeyCallback() {
      return this.glfwKeyCallback;
    }

    /**
     * Checks if a specific keyboard key is currently pressed.
     *
     * <p>Validates the key code to prevent array index out of bounds.
     * Returns false for invalid key codes.
     *
     * @param _keyCode the GLFW key code to check (GLFW_KEY_*)
     * @return {@code true} if the key is pressed, {@code false} otherwise
     */
    public boolean isKeyPressed(
        final int _keyCode) {
      /* Validate key code to prevent array index out of bounds. */
      if ((_keyCode >= 0)
       && (_keyCode < GLFW.GLFW_KEY_LAST)) {
        return this.keyPressed[_keyCode];
      }
      return false;
    }

    /**
     * Updates the pressed state of a specific keyboard key.
     *
     * <p>Called internally by {@link KeyCallback} when key events occur.
     * Validates the key code to prevent array index out of bounds.
     *
     * @param _keyCode the GLFW key code to update (GLFW_KEY_*)
     * @param _keyStatus {@code true} if pressed, {@code false} if released
     */
    public void setKeyPressed(
        final int _keyCode,
        final boolean _keyStatus) {
      /* Validate key code to prevent array index out of bounds. */
      if ((_keyCode >= 0)
       && (_keyCode < GLFW.GLFW_KEY_LAST)) {
        this.keyPressed[_keyCode] = _keyStatus;
      }
    }

  }

