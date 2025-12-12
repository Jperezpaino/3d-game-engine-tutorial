package es.noa.rad.game.engine.event;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import es.noa.rad.game.engine.event.callback.CursorPosCallback;
import es.noa.rad.game.engine.event.callback.MouseButtonCallback;
import es.noa.rad.game.engine.event.callback.ScrollCallback;

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
    private final GLFWCursorPosCallback glfwCursorPosCallback;

    /**
     *
     */
    private final GLFWScrollCallback glfwScrollCallback;

    /**
     *
     */
    private final GLFWMouseButtonCallback glfwMouseButtonCallback;

    /**
     *
     */
    private double cursorPositionX;

    /**
     *
     */
    private double cursorPositionY;

    /**
     *
     */
    private double cursorScrollX;

    /**
     *
     */
    private double cursorScrollY;

    /**
     *
     */
    private final boolean[] mouseButtonPressed;

    /**
     *
     */
    private MouseEventHandler() {
      this.cursorPositionX = 0.0d;
      this.cursorPositionY = 0.0d;
      this.cursorScrollX = 0.0d;
      this.cursorScrollY = 0.0d;
      this.mouseButtonPressed = new boolean[GLFW.GLFW_MOUSE_BUTTON_LAST];
      this.glfwCursorPosCallback = new CursorPosCallback();
      this.glfwScrollCallback = new ScrollCallback();
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
      this.glfwCursorPosCallback.free();
      this.glfwScrollCallback.free();
      this.glfwMouseButtonCallback.free();
    }

    /**
     *
     * @return {@code GLFWCursorPosCallback}
     */
    public GLFWCursorPosCallback getGlfwCursorPosCallback() {
      return this.glfwCursorPosCallback;
    }

    /**
     *
     * @return {@code GLFWScrollCallback}
     */
    public GLFWScrollCallback getGlfwScrollCallback() {
      return this.glfwScrollCallback;
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
     * @return {@code double}
     */
    public double getCursorPositionX() {
      return this.cursorPositionX;
    }

    /**
     *
     * @param _cursorPositionX {@code double}
     */
    public void setCursorPositionX(
        final double _cursorPositionX) {
      this.cursorPositionX = _cursorPositionX;
    }

    /**
     *
     * @return {@code double}
     */
    public double getCursorPositionY() {
      return this.cursorPositionY;
    }

    /**
     *
     * @param _cursorPositionY {@code double}
     */
    public void setCursorPositionY(
        final double _cursorPositionY) {
      this.cursorPositionY = _cursorPositionY;
    }

    /**
     *
     * @return {@code double}
     */
    public double getCursorScrollX() {
      return this.cursorScrollX;
    }

    /**
     *
     * @param _cursorScrollX {@code double}
     */
    public void setCursorScrollX(
        final double _cursorScrollX) {
      this.cursorScrollX = _cursorScrollX;
    }

    /**
     *
     * @param _cursorScrollX {@code double}
     */
    public void addCursorScrollX(
        final double _cursorScrollX) {
      this.cursorScrollX += _cursorScrollX;
    }

    /**
     *
     * @return {@code double}
     */
    public double getCursorScrollY() {
      return this.cursorScrollY;
    }

    /**
     *
     * @param _cursorScrollY {@code double}
     */
    public void setCursorScrollY(
        final double _cursorScrollY) {
      this.cursorScrollY = _cursorScrollY;
    }

    /**
     *
     * @param _cursorScrollY {@code double}
     */
    public void addCursorScrollY(
        final double _cursorScrollY) {
      this.cursorScrollY += _cursorScrollY;
    }

    /**
     *
     * @param _buttonCode {@code int}
     * @return {@code boolean}
     */
    public boolean isMouseButtonPressed(
        final int _buttonCode) {
      if ((_buttonCode >= 0)
       && (_buttonCode < GLFW.GLFW_MOUSE_BUTTON_LAST)) {
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
      if ((_buttonCode >= 0)
       && (_buttonCode < GLFW.GLFW_MOUSE_BUTTON_LAST)) {
        this.mouseButtonPressed[_buttonCode] = _buttonStatus;
      }
    }

  }
