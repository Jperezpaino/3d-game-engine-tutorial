package es.noa.rad.game.engine.configuration.settings;

import es.noa.rad.game.engine.configuration.Configuration;

  /**
   * Enumeration of window-related configuration settings.
   *
   * <p>This enum provides type-safe access to window configuration
   * properties from {@code application.properties}. Each constant
   * includes its property key, expected type, and default value.
   *
   * <p>Usage example:
   * <pre>{@code
   * int width = WindowSettings.WINDOW_WIDTH.get();
   * String title = WindowSettings.WINDOW_TITLE.get("My Game");
   * }</pre>
   *
   * @see Configuration
   * @see GameSettings
   */
  public enum WindowSettings {

    /**
     * Window width in pixels.
     *
     * <p>Property key: {@code window.width}
     * <p>Type: {@code Integer}
     * <p>Default: {@code 1280}
     */
    WINDOW_WIDTH(
      "window.width",
      Integer.class,
      1280
    ),

    /**
     * Window height in pixels.
     *
     * <p>Property key: {@code window.height}
     * <p>Type: {@code Integer}
     * <p>Default: {@code 720}
     */
    WINDOW_HEIGHT(
      "window.height",
      Integer.class,
      720
    ),

    /**
     * Window title text displayed in the title bar.
     *
     * <p>Property key: {@code window.title}
     * <p>Type: {@code String}
     * <p>Default: {@code "3D Game Engine"}
     */
    WINDOW_TITLE(
      "window.title",
      String.class,
      "3D Game Engine"
    );

    /**
     * Property key in application.properties file.
     */
    private final String property;

    /**
     * Java class type for automatic conversion.
     */
    private final Class<?> classType;

    /**
     * Default value if property is not found in configuration.
     */
    private final Object defaultValue;

    /**
     * Private constructor for enum constants.
     *
     * @param _property the property key
     * @param _classType the expected type
     * @param _defaultValue the fallback value
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
     * Gets the property value from configuration with enum default.
     *
     * <p>Returns the value from {@code application.properties},
     * or the default value defined in this enum if not found.
     *
     * @param <T> the type of the property value
     * @return the property value converted to the expected type
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
     * Gets the property value with a custom default.
     *
     * <p>Allows overriding the enum's default value with a custom one.
     * Useful for runtime-specific defaults.
     *
     * @param <T> the type of the property value
     * @param _defaultValue custom default to use if property not found
     * @return the property value or custom default
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
