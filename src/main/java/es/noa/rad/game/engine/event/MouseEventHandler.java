package es.noa.rad.game.engine.event;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

import es.noa.rad.game.engine.event.callback.MouseButtonCallback;

  /**
   *
   */
  public final class MouseEventHandler {

    /**
     *
     */
    private static MouseEventHandler instance = null;

    /**
     *
     */
    private final GLFWMouseButtonCallback glfwMouseButtonCallback;

    /**
     *
     */
    private final boolean[] mouseButtonPressed;

    /**
     *
     */
    private MouseEventHandler() {
      this.mouseButtonPressed = new boolean[GLFW.GLFW_MOUSE_BUTTON_LAST];
      this.glfwMouseButtonCallback = new MouseButtonCallback();
    }

    /**
     *
     */
    private static synchronized void createInstance() {
      /*
       * Synchronized creator to protect against possible multi-threading
       * problems.
       */
      if (MouseEventHandler.instance == null) {
        MouseEventHandler.instance = new MouseEventHandler();
      }
    }

    /**
     *
     * @return {@code MouseEventHandler}
     */
    public static MouseEventHandler get() {
      if (MouseEventHandler.instance == null) {
        MouseEventHandler.createInstance();
      }
      return MouseEventHandler.instance;
    }

    /**
     *
     */
    public void close() {
      this.glfwMouseButtonCallback.free();
    }

    /**
     *
     * @return {@code GLFWMouseButtonCallback}
     */
    public GLFWMouseButtonCallback getGlfwMouseButtonCallback() {
      return this.glfwMouseButtonCallback;
    }

    /**
     *
     * @param _buttonCode {@code int}
     * @return {@code boolean}
     */
    public boolean isMouseButtonPressed(
        final int _buttonCode) {
      if (_buttonCode < GLFW.GLFW_MOUSE_BUTTON_LAST) {
        return this.mouseButtonPressed[_buttonCode];
      }
      return false;
    }

    /**
     *
     * @param _buttonCode {@code int}
     * @param _buttonStatus {@code boolean}
     */
    public void setMouseButtonPressed(
        final int _buttonCode,
        final boolean _buttonStatus) {
      if (_buttonCode < GLFW.GLFW_MOUSE_BUTTON_LAST) {
        this.mouseButtonPressed[_buttonCode] = _buttonStatus;
      }
    }

  }
