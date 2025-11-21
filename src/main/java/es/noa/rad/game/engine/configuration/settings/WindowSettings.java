package es.noa.rad.game.engine.configuration.settings;

import es.noa.rad.game.engine.configuration.Configuration;

  /**
   *
   */
  public enum WindowSettings {

    /**
     *
     */
    WINDOW_WIDTH(
      "window.width",
      Integer.class
    ),

    /**
     *
     */
    WINDOW_HEIGHT(
      "window.height",
      Integer.class
    ),

    /**
     *
     */
    WINDOW_TITLE(
      "window.title",
      String.class
    );

    /**
     *
     */
    private final String property;

    /**
     *
     */
    private final Class<?> classType;

    /**
     *
     * @param _property {@code String}
     * @param _classType {@code Class<T>}
     */
    WindowSettings(
        final String _property,
        final Class<?> _classType) {
      this.property = _property;
      this.classType = _classType;
    }

    /**
     *
     * @param <T> {@code <T>}
     * @return {@code <T>}
     */
    @SuppressWarnings("unchecked")
    public <T> T get() {
      return (T) Configuration.get()
        .property(this.property, (Class<T>) this.classType);
    }

    /**
     *
     * @param <T> {@code <T>}
     * @param _defaultValue {@code <T>}
     * @return {@code <T>}
     */
    @SuppressWarnings("unchecked")
    public <T> T get(
        final T _defaultValue) {
      return (T) Configuration.get()
        .property(this.property, (Class<T>) this.classType, (T) _defaultValue);
    }

  }
