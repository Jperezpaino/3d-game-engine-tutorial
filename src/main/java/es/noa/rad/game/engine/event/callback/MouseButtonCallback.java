package es.noa.rad.game.engine.event.callback;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

import es.noa.rad.game.engine.event.MouseEventHandler;

  /**
   *
   */
  public final class MouseButtonCallback
      extends GLFWMouseButtonCallback {

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public void invoke(
        final long _window,
        final int _button,
        final int _action,
        final int _modifier) {
      MouseEventHandler.get()
        .setMouseButtonPressed(_button, (_action != GLFW.GLFW_RELEASE));
    }

  }
