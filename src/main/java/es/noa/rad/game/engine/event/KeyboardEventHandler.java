package es.noa.rad.game.engine.event;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;
import es.noa.rad.game.engine.event.callback.KeyCallback;

  /**
   *
   */
  public final class KeyboardEventHandler {

    /**
     *
     */
    private static KeyboardEventHandler instance = null;

    /**
     *
     */
    private final GLFWKeyCallback glfwKeyCallback;

    /**
     *
     */
    private final boolean[] keyPressed;

    /**
     *
     */
    private KeyboardEventHandler() {
      this.keyPressed = new boolean[GLFW.GLFW_KEY_LAST];
      this.glfwKeyCallback = new KeyCallback();
    }

    /**
     *
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
     *
     * @return {@code KeyboardEventHandler}
     */
    public static KeyboardEventHandler get() {
      if (KeyboardEventHandler.instance == null) {
        KeyboardEventHandler.createInstance();
      }
      return KeyboardEventHandler.instance;
    }

    /**
     *
     */
    public void close() {
      this.glfwKeyCallback.free();
    }

    /**
     *
     * @return {@code GLFWKeyCallback}
     */
    public GLFWKeyCallback getGlfwKeyCallback() {
      return this.glfwKeyCallback;
    }

    /**
     *
     * @param _keyCode {@code int}
     * @return {@code boolean}
     */
    public boolean isKeyPressed(
        final int _keyCode) {
      if ((_keyCode >= 0)
       && (_keyCode < GLFW.GLFW_KEY_LAST)) {
        return this.keyPressed[_keyCode];
      }
      return false;
    }

    /**
     *
     * @param _keyCode {@code int}
     * @param _keyStatus {@code boolean}
     */
    public void setKeyPressed(
        final int _keyCode,
        final boolean _keyStatus) {
      if ((_keyCode >= 0)
       && (_keyCode < GLFW.GLFW_KEY_LAST)) {
        this.keyPressed[_keyCode] = _keyStatus;
      }
    }

  }
