package es.noa.rad.game.engine.event.callback;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;
import es.noa.rad.game.engine.event.KeyboardEventHandler;

  /**
   *
   */
  public final class KeyCallback
      extends GLFWKeyCallback {

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public void invoke(
        final long _window,
        final int _keyCode,
        final int _scanCode,
        final int _action,
        final int _modifier) {
      if (_keyCode != GLFW.GLFW_KEY_UNKNOWN) {
        KeyboardEventHandler.get()
          .setKeyPressed(_keyCode, (_action != GLFW.GLFW_RELEASE));
      }
    }

  }
