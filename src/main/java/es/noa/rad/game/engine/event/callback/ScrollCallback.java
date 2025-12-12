package es.noa.rad.game.engine.event.callback;

import org.lwjgl.glfw.GLFWScrollCallback;
import es.noa.rad.game.engine.event.MouseEventHandler;

  /**
   * GLFW scroll wheel callback handler.
   *
   * <p>This callback is invoked by GLFW whenever the scroll wheel is moved.
   * It accumulates scroll offsets in {@link MouseEventHandler}.
   *
   * <p>Scroll behavior:
   * <ul>
   *   <li>Positive Y offset: Scroll up (towards user)</li>
   *   <li>Negative Y offset: Scroll down (away from user)</li>
   *   <li>Positive X offset: Scroll right (trackpads)</li>
   *   <li>Negative X offset: Scroll left (trackpads)</li>
   * </ul>
   *
   * <p>Values are accumulative and designed for global zoom control.
   * Typical scroll "notch" value is ±1.0, but may vary by hardware/OS.
   *
   * @see MouseEventHandler
   * @see GLFWScrollCallback
   */
  public final class ScrollCallback
      extends GLFWScrollCallback {

    /**
     * Processes scroll wheel events from GLFW.
     *
     * <p>Adds the scroll offsets to the accumulated scroll values
     * in MouseEventHandler (cumulative, not reset per frame).
     *
     * {@inheritDoc}
     *
     * @param _window the window that received the event
     * @param _xOffSet the scroll offset along the x-axis
     * @param _yOffSet the scroll offset along the y-axis
     */
    @Override
    public void invoke(
        final long _window,
        final double _xOffSet,
        final double _yOffSet) {
      /* Accumulate scroll offsets for global zoom/pan control. */
      MouseEventHandler.get().addCursorScrollX(_xOffSet);
      MouseEventHandler.get().addCursorScrollY(_yOffSet);
    }

  }

