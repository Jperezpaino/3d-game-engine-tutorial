package es.noa.rad.game.engine.event.callback;

import org.lwjgl.glfw.GLFWScrollCallback;
import es.noa.rad.game.engine.event.MouseEventHandler;

  /**
   *
   */
  public final class ScrollCallback
      extends GLFWScrollCallback {

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public void invoke(
        final long _window,
        final double _xOffSet,
        final double _yOffSet) {
      MouseEventHandler.get().addCursorScrollX(_xOffSet);
      MouseEventHandler.get().addCursorScrollY(_yOffSet);
    }

  }
