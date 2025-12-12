package es.noa.rad.game.engine.event;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import es.noa.rad.game.engine.event.callback.CursorPosCallback;
import es.noa.rad.game.engine.event.callback.MouseButtonCallback;
import es.noa.rad.game.engine.event.callback.ScrollCallback;

  /**
   * Singleton handler for mouse input events.
   *
   * <p>This class manages all mouse-related state including:
   * <ul>
   *   <li>Cursor position (X/Y coordinates in window space)</li>
   *   <li>Scroll offset (X/Y cumulative scroll for zoom/pan)</li>
   *   <li>Mouse button states (pressed/released)</li>
   * </ul>
   *
   * <p>Thread-safe singleton implementation with lazy initialization.
   * The mouse state is updated automatically by GLFW callbacks:
   * {@link CursorPosCallback}, {@link ScrollCallback}, and
   * {@link MouseButtonCallback}.
   *
   * <p>Usage example:
   * <pre>{@code
   * MouseEventHandler mouse = MouseEventHandler.get();
   * if (mouse.isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
   *     double x = mouse.getCursorPositionX();
   *     double y = mouse.getCursorPositionY();
   *     // Handle left click at position (x, y)
   * }
   * }</pre>
   *
   * @see CursorPosCallback
   * @see ScrollCallback
   * @see MouseButtonCallback
   */
  public final class MouseEventHandler {

    /**
     * Singleton instance of the mouse event handler.
     */
    private static MouseEventHandler instance = null;

    /**
     * GLFW cursor position callback that tracks mouse movement.
     */
    private final GLFWCursorPosCallback glfwCursorPosCallback;

    /**
     * GLFW scroll callback that tracks scroll wheel events.
     */
    private final GLFWScrollCallback glfwScrollCallback;

    /**
     * GLFW mouse button callback that tracks button press/release events.
     */
    private final GLFWMouseButtonCallback glfwMouseButtonCallback;

    /**
     * Current horizontal cursor position in window coordinates.
     * Origin (0,0) is at the top-left corner of the window.
     */
    private double cursorPositionX;

    /**
     * Current vertical cursor position in window coordinates.
     * Origin (0,0) is at the top-left corner of the window.
     */
    private double cursorPositionY;

    /**
     * Cumulative horizontal scroll offset.
     * Typically used for horizontal panning in applications that support it.
     */
    private double cursorScrollX;

    /**
     * Cumulative vertical scroll offset.
     * Positive values indicate scrolling up, negative values scrolling down.
     * Typically used for zoom control in camera systems.
     */
    private double cursorScrollY;

    /**
     * Array storing the pressed state of all mouse buttons.
     * Index corresponds to GLFW button codes (GLFW_MOUSE_BUTTON_*).
     * Size is GLFW_MOUSE_BUTTON_LAST to cover all possible buttons.
     */
    private final boolean[] mouseButtonPressed;

    /**
     * Private constructor to enforce singleton pattern.
     * Initializes all state variables and creates GLFW callbacks.
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
     * Creates the singleton instance in a thread-safe manner.
     * Uses synchronized method to prevent multiple instances
     * in multi-threaded environments.
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
     * Gets the singleton instance of the mouse event handler.
     * Creates the instance on first call (lazy initialization).
     *
     * @return the singleton {@code MouseEventHandler} instance
     */
    public static MouseEventHandler get() {
      if (MouseEventHandler.instance == null) {
        MouseEventHandler.createInstance();
      }
      return MouseEventHandler.instance;
    }

    /**
     * Releases all GLFW callback resources.
     * Should be called when shutting down the application
     * to prevent memory leaks.
     */
    public void close() {
      this.glfwCursorPosCallback.free();
      this.glfwScrollCallback.free();
      this.glfwMouseButtonCallback.free();
    }

    /**
     * Gets the GLFW cursor position callback for registration with GLFW.
     * This callback should be registered with glfwSetCursorPosCallback().
     *
     * @return the {@code GLFWCursorPosCallback} instance
     */
    public GLFWCursorPosCallback getGlfwCursorPosCallback() {
      return this.glfwCursorPosCallback;
    }

    /**
     * Gets the GLFW scroll callback for registration with GLFW.
     * This callback should be registered with glfwSetScrollCallback().
     *
     * @return the {@code GLFWScrollCallback} instance
     */
    public GLFWScrollCallback getGlfwScrollCallback() {
      return this.glfwScrollCallback;
    }

    /**
     * Gets the GLFW mouse button callback for registration with GLFW.
     * This callback should be registered with glfwSetMouseButtonCallback().
     *
     * @return the {@code GLFWMouseButtonCallback} instance
     */
    public GLFWMouseButtonCallback getGlfwMouseButtonCallback() {
      return this.glfwMouseButtonCallback;
    }

    /**
     * Gets the current horizontal cursor position in window coordinates.
     *
     * @return the X coordinate of the cursor
     */
    public double getCursorPositionX() {
      return this.cursorPositionX;
    }

    /**
     * Sets the horizontal cursor position.
     * Called internally by {@link CursorPosCallback}.
     *
     * @param _cursorPositionX the new X coordinate
     */
    public void setCursorPositionX(
        final double _cursorPositionX) {
      this.cursorPositionX = _cursorPositionX;
    }

    /**
     * Gets the current vertical cursor position in window coordinates.
     *
     * @return the Y coordinate of the cursor
     */
    public double getCursorPositionY() {
      return this.cursorPositionY;
    }

    /**
     * Sets the vertical cursor position.
     * Called internally by {@link CursorPosCallback}.
     *
     * @param _cursorPositionY the new Y coordinate
     */
    public void setCursorPositionY(
        final double _cursorPositionY) {
      this.cursorPositionY = _cursorPositionY;
    }

    /**
     * Gets the cumulative horizontal scroll offset.
     *
     * @return the X scroll offset
     */
    public double getCursorScrollX() {
      return this.cursorScrollX;
    }

    /**
     * Sets the horizontal scroll offset.
     * Replaces the current value with a new absolute offset.
     *
     * @param _cursorScrollX the new X scroll offset
     */
    public void setCursorScrollX(
        final double _cursorScrollX) {
      this.cursorScrollX = _cursorScrollX;
    }

    /**
     * Adds to the horizontal scroll offset.
     * Called internally by {@link ScrollCallback} to accumulate scroll events.
     *
     * @param _cursorScrollX the scroll delta to add
     */
    public void addCursorScrollX(
        final double _cursorScrollX) {
      /* Accumulate scroll offset for cumulative control. */
      this.cursorScrollX += _cursorScrollX;
    }

    /**
     * Gets the cumulative vertical scroll offset.
     * Positive values indicate scrolling up, negative values scrolling down.
     *
     * @return the Y scroll offset
     */
    public double getCursorScrollY() {
      return this.cursorScrollY;
    }

    /**
     * Sets the vertical scroll offset.
     * Replaces the current value with a new absolute offset.
     *
     * @param _cursorScrollY the new Y scroll offset
     */
    public void setCursorScrollY(
        final double _cursorScrollY) {
      this.cursorScrollY = _cursorScrollY;
    }

    /**
     * Adds to the vertical scroll offset.
     * Called internally by {@link ScrollCallback} to accumulate scroll events.
     *
     * @param _cursorScrollY the scroll delta to add
     */
    public void addCursorScrollY(
        final double _cursorScrollY) {
      /* Accumulate scroll offset for cumulative control. */
      this.cursorScrollY += _cursorScrollY;
    }

    /**
     * Checks if a specific mouse button is currently pressed.
     *
     * <p>Validates the button code to prevent array index out of bounds.
     * Returns false for invalid button codes.
     *
     * @param _buttonCode the GLFW button code to check (GLFW_MOUSE_BUTTON_*)
     * @return {@code true} if the button is pressed, {@code false} otherwise
     */
    public boolean isMouseButtonPressed(
        final int _buttonCode) {
      /* Validate button code to prevent array index out of bounds. */
      if ((_buttonCode >= 0)
       && (_buttonCode < GLFW.GLFW_MOUSE_BUTTON_LAST)) {
        return this.mouseButtonPressed[_buttonCode];
      }
      return false;
    }

    /**
     * Updates the pressed state of a specific mouse button.
     *
     * <p>Called internally by {@link MouseButtonCallback} when button events occur.
     * Validates the button code to prevent array index out of bounds.
     *
     * @param _buttonCode the GLFW button code to update (GLFW_MOUSE_BUTTON_*)
     * @param _buttonStatus {@code true} if pressed, {@code false} if released
     */
    public void setMouseButtonPressed(
        final int _buttonCode,
        final boolean _buttonStatus) {
      /* Validate button code to prevent array index out of bounds. */
      if ((_buttonCode >= 0)
       && (_buttonCode < GLFW.GLFW_MOUSE_BUTTON_LAST)) {
        this.mouseButtonPressed[_buttonCode] = _buttonStatus;
      }
    }

  }
