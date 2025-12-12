package es.noa.rad.game.engine.event.callback;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

import es.noa.rad.game.engine.event.MouseEventHandler;

  /**
   * GLFW mouse button callback handler.
   *
   * <p>This callback is invoked by GLFW whenever a mouse button event occurs
   * (button press or release). It updates the mouse button state in
   * {@link MouseEventHandler}.
   *
   * @see MouseEventHandler
   * @see GLFWMouseButtonCallback
   */
  public final class MouseButtonCallback
      extends GLFWMouseButtonCallback {

    /**
     * Processes mouse button input events from GLFW.
     *
     * <p>Updates the mouse button state based on the action:
     * <ul>
     *   <li>GLFW_PRESS: Button is pressed (true)</li>
     *   <li>GLFW_RELEASE: Button is released (false)</li>
     * </ul>
     *
     * {@inheritDoc}
     *
     * @param _window the window that received the event
     * @param _button the mouse button code (GLFW_MOUSE_BUTTON_*)
     * @param _action the button action (GLFW_PRESS or GLFW_RELEASE)
     * @param _modifier bit field describing which modifier keys were held down
     */
    @Override
    public void invoke(
        final long _window,
        final int _button,
        final int _action,
        final int _modifier) {
      /* Update button state: pressed if action is not RELEASE. */
      MouseEventHandler.get()
        .setMouseButtonPressed(_button, (_action != GLFW.GLFW_RELEASE));
    }

  }

