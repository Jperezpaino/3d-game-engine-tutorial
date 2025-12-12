package es.noa.rad.game.engine.event.callback;

import org.lwjgl.glfw.GLFWCursorPosCallback;

import es.noa.rad.game.engine.event.MouseEventHandler;

  /**
   * GLFW cursor position callback handler.
   *
   * <p>This callback is invoked by GLFW whenever the cursor moves within
   * the window. It updates the cursor position in {@link MouseEventHandler}.
   *
   * <p>Coordinates are in pixels, with origin (0,0) at top-left corner.
   * X increases to the right, Y increases downward.
   *
   * @see MouseEventHandler
   * @see GLFWCursorPosCallback
   */
  public final class CursorPosCallback
      extends GLFWCursorPosCallback {

    /**
     * Processes cursor position events from GLFW.
     *
     * <p>Updates the stored cursor position coordinates in the
     * MouseEventHandler singleton.
     *
     * {@inheritDoc}
     *
     * @param _window the window that received the event
     * @param _xPosition the new x-coordinate of the cursor, in pixels
     * @param _yPosition the new y-coordinate of the cursor, in pixels
     */
    @Override
    public void invoke(
        final long _window,
        final double _xPosition,
        final double _yPosition) {
      /* Update cursor position in MouseEventHandler. */
      MouseEventHandler.get().setCursorPositionX(_xPosition);
      MouseEventHandler.get().setCursorPositionY(_yPosition);
    }

  }

