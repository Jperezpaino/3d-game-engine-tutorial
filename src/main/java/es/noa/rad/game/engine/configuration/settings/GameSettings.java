package es.noa.rad.game.engine.configuration.settings;

import es.noa.rad.game.engine.configuration.Configuration;

  /**
   * Enumeration of game loop and timing configuration settings.
   *
   * <p>This enum provides type-safe access to game engine configuration
   * properties from {@code application.properties}. Each constant
   * includes its property key, expected type, and default value.
   *
   * <p>Configuration includes:
   * <ul>
   *   <li>VSync control</li>
   *   <li>FPS limiting when VSync is disabled</li>
   *   <li>Fixed timestep update rate (UPS)</li>
   *   <li>Spiral of death protection parameters</li>
   * </ul>
   *
   * <p>Usage example:
   * <pre>{@code
   * boolean vsync = GameSettings.GAME_VERTICAL_SYNCHRONIZATION.get();
   * double ups = GameSettings.GAME_UPDATES_PER_SECOND.get();
   * }</pre>
   *
   * @see Configuration
   * @see WindowSettings
   */
  public enum GameSettings {

    /**
     * Enable or disable vertical synchronization (VSync).
     *
     * <p>When enabled, frame rate is limited to monitor refresh rate
     * and {@link #GAME_MAXIMUM_FRAMES_PER_SECOND} is ignored.
     *
     * <p>Property key: {@code game.vertical.synchronization}
     * <p>Type: {@code Boolean}
     * <p>Default: {@code true}
     */
    GAME_VERTICAL_SYNCHRONIZATION(
      "game.vertical.synchronization",
      Boolean.class,
      true
    ),

    /**
     * Maximum frames per second when VSync is disabled.
     *
     * <p>This value caps the frame rate to prevent excessive CPU/GPU
     * usage and overheating. Only applies when VSync is disabled.
     * Set to 0 for unlimited FPS (not recommended).
     *
     * <p>Property key: {@code game.maximum.frames.per.second}
     * <p>Type: {@code Integer}
     * <p>Default: {@code 144}
     */
    GAME_MAXIMUM_FRAMES_PER_SECOND(
      "game.maximum.frames.per.second",
      Integer.class,
      144
    ),

    /**
     * Target updates per second for the game loop (fixed timestep).
     *
     * <p>Determines how often game logic updates are executed.
     * Higher values provide smoother physics but require more CPU.
     * Standard value is 60 UPS to match common monitor refresh rates.
     *
     * <p>Property key: {@code game.updates.per.second}
     * <p>Type: {@code Double}
     * <p>Default: {@code 60.0}
     */
    GAME_UPDATES_PER_SECOND(
      "game.updates.per.second",
      Double.class,
      60.0D
    ),

    /**
     * Maximum number of updates allowed per frame.
     *
     * <p>Prevents the "spiral of death" where the game cannot catch up
     * with accumulated time. If more updates are needed, accumulated
     * time is capped instead.
     *
     * <p>Property key: {@code game.maximum.updates.per.frame}
     * <p>Type: {@code Integer}
     * <p>Default: {@code 5}
     */
    GAME_MAXIMUM_UPDATES_PER_FRAME(
      "game.maximum.updates.per.frame",
      Integer.class,
      5
    ),

    /**
     * Maximum accumulated time in seconds before resetting.
     *
     * <p>If accumulated time exceeds this threshold (e.g., during
     * heavy lag or debugging), it is reset to prevent the spiral
     * of death. This avoids the game trying to "catch up" with
     * hundreds of missed updates.
     *
     * <p>Property key: {@code game.maximum.accumulated.time}
     * <p>Type: {@code Float}
     * <p>Default: {@code 0.5f} (500ms)
     */
    GAME_MAXIMUM_ACCUMULATED_TIME(
      "game.maximum.accumulated.time",
      Float.class,
      0.5F
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
    GameSettings(
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
