package es.noa.rad.game.engine.event.callback;

import org.lwjgl.glfw.GLFWCursorPosCallback;

import es.noa.rad.game.engine.event.MouseEventHandler;

  /**
   *
   */
  public final class CursorPosCallback
      extends GLFWCursorPosCallback {

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public void invoke(
        final long _window,
        final double _xPosition,
        final double _yPosition) {
      MouseEventHandler.get().setCursorPositionX(_xPosition);
      MouseEventHandler.get().setCursorPositionY(_yPosition);
    }

  }
