package es.noa.rad.game.engine.event.callback;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;

import es.noa.rad.game.engine.event.KeyboardEventHandler;

  /**
   * GLFW keyboard callback handler.
   *
   * <p>This callback is invoked by GLFW whenever a keyboard event occurs
   * (key press, release, or repeat). It updates the keyboard state in
   * {@link KeyboardEventHandler}.
   *
   * <p>Ignores unknown keys (GLFW_KEY_UNKNOWN) to prevent invalid state.
   *
   * @see KeyboardEventHandler
   * @see GLFWKeyCallback
   */
  public final class KeyCallback
      extends GLFWKeyCallback {

    /**
     * Processes keyboard input events from GLFW.
     *
     * <p>Updates the keyboard state based on the action:
     * <ul>
     *   <li>GLFW_PRESS or GLFW_REPEAT: Key is pressed (true)</li>
     *   <li>GLFW_RELEASE: Key is released (false)</li>
     * </ul>
     *
     * {@inheritDoc}
     *
     * @param _window the window that received the event
     * @param _keyCode the keyboard key code (GLFW_KEY_*)
     * @param _scanCode the platform-specific scancode of the key
     * @param _action the key action (GLFW_PRESS, GLFW_RELEASE, GLFW_REPEAT)
     * @param _modifier bit field describing which modifier keys were held down
     */
    @Override
    public void invoke(
        final long _window,
        final int _keyCode,
        final int _scanCode,
        final int _action,
        final int _modifier) {
      /* Ignore unknown keys to prevent invalid state updates. */
      if (_keyCode != GLFW.GLFW_KEY_UNKNOWN) {
        KeyboardEventHandler.get()
          .setKeyPressed(_keyCode, (_action != GLFW.GLFW_RELEASE));
      }
    }

  }

