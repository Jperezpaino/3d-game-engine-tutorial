package es.noa.rad.game.engine.configuration.settings;

import es.noa.rad.game.engine.configuration.Configuration;

  /**
   *
   */
  public enum WindowSettings {

    /**
     * Window width in pixels.
     *
     * Default: {@code 1280}
     */
    WINDOW_WIDTH(
      "window.width",
      Integer.class,
      1280
    ),

    /**
     * Window height in pixels.
     *
     * Default: {@code 720}
     */
    WINDOW_HEIGHT(
      "window.height",
      Integer.class,
      720
    ),

    /**
     * Window title text.
     *
     * Default: {@code "3D Game Engine"}
     */
    WINDOW_TITLE(
      "window.title",
      String.class,
      "3D Game Engine"
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
     */
    private final Object defaultValue;

    /**
     *
     * @param _property {@code String}
     * @param _classType {@code Class<T>}
     * @param _defaultValue {@code Object}
     */
    WindowSettings(
        final String _property,
        final Class<?> _classType,
        final Object _defaultValue) {
      this.property = _property;
      this.classType = _classType;
      this.defaultValue = _defaultValue;
    }

    /**
     *
     * @param <T> {@code <T>}
     * @return {@code <T>}
     */
    @SuppressWarnings("unchecked")
    public <T> T get() {
      return (T) Configuration.get()
        .property(
          this.property,
          (Class<T>) this.classType,
          (T) this.defaultValue
        );
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
      /* Establish which default value to use. */
      T propertyValue = (T) this.defaultValue;
      if (_defaultValue != null) {
        propertyValue = _defaultValue;
      }

      return (T) Configuration.get()
        .property(
          this.property,
          (Class<T>) this.classType,
          propertyValue
        );
    }

  }
