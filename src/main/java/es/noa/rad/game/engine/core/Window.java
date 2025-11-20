package es.noa.rad.game.engine.core;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryUtil;

  /**
   *
   */
  public final class Window {

    /**
     *
     */
    private static Window instance = null;

    /**
     *
     */
    private long glfwWindow;

    /**
     *
     */
    private int width;

    /**
     *
     */
    private int height;

    /**
     *
     */
    private String title;

    /**
     *
     */
    private Window() {
      this.width = 0;
      this.height = 0;
      this.title = "";
      this.glfwWindow = MemoryUtil.NULL;
    }

    /**
     *
     */
    private static synchronized void createInstance() {
      /*
       * Synchronized creator to protect against possible multi-threading
       * problems.
       */
      if (Window.instance == null) {
        Window.instance = new Window();
      }
    }

    /**
     *
     * @return {@code Window}
     */
    public static Window get() {
      if (Window.instance == null) {
        Window.createInstance();
      }
      return Window.instance;
    }

    /**
     *
     * @param _width {@code int}
     * @param _height {@code int}
     * @param _title {@code String}
     */
    public void init(
        final int _width,
        final int _height,
        final String _title) {
      System.out.println("Initializing Game!");

      this.width = _width;
      this.height = _height;
      this.title = _title;

      /*
       * Initialize GLFW. Most GLFW functions will not work before doing this.
       */
      if (!GLFW.glfwInit()) {
        System.err.println("ERROR: GLFW wasn't initializied");
        return;
      }

      /* Create the window. */
      this.glfwWindow =
        GLFW.glfwCreateWindow(
          this.width,
          this.height,
          this.title,
          MemoryUtil.NULL,
          MemoryUtil.NULL
        );

      if (this.glfwWindow == MemoryUtil.NULL) {
        System.err.println("ERROR: Window wasn't created");
        return;
      }

      final GLFWVidMode videoMode
        = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());

      final int centerX
        = ((videoMode.width() - this.width) / 2);
      final int centerY
        = ((videoMode.height() - this.height) / 2);

      /* Make the window position in the center of the screen. */
      GLFW.glfwSetWindowPos(this.glfwWindow, centerX, centerY);

      /* Make the window visible. */
      GLFW.glfwShowWindow(this.glfwWindow);
    }

    /**
     *
     */
    public void update() {
      System.out.println("Updating Game!");

      /*
       * Poll for window events. The key callback above will only be invoked
       * during this call.
       */
      GLFW.glfwPollEvents();
    }

    /**
     *
     */
    public void render() {
      System.out.println("Rendering Game!");

      /* Swap the window buffers. */
      GLFW.glfwSwapBuffers(this.glfwWindow);
    }

    /**
     *
     * @return {@code boolean}
     */
    public boolean shouldClose() {
      /* Check if the user has attempted to close the window. */
      return GLFW.glfwWindowShouldClose(this.glfwWindow);
    }

    /**
     *
     * @return {@code long}
     */
    public long glfwWindow() {
      return this.glfwWindow;
    }

    /**
     *
     * @param _glfwWindow {@code long}
     */
    public void glfwWindow(
        final long _glfwWindow) {
      this.glfwWindow = _glfwWindow;
    }

    /**
     *
     * @return {@code int}
     */
    public int width() {
      return this.width;
    }

    /**
     *
     * @param _width {@code int}
     */
    public void width(
        final int _width) {
      this.width = _width;
    }

    /**
     *
     * @return {@code int}
     */
    public int height() {
      return this.height;
    }

    /**
     *
     * @param _height {@code int}
     */
    public void height(
        final int _height) {
      this.height = _height;
    }

    /**
     *
     * @return {@code String}
     */
    public String title() {
      return this.title;
    }

    /**
     *
     * @param _title {@code String}
     */
    public void title(
        final String _title) {
      this.title = _title;
    }

  }
