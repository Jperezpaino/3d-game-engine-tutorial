package es.noa.rad.game.engine.configuration.settings;

import es.noa.rad.game.engine.configuration.Configuration;

  /**
   *
   */
  public enum GameSettings {

    /**
     * Enable or disable vertical synchronization (VSync).
     *
     * Default: {@code true}
     */
    GAME_VERTICAL_SYNCHRONIZATION(
      "game.vertical.synchronization",
      Boolean.class,
      true
    ),

    /**
     * Target updates per second for the game loop (fixed timestep).
     *
     * Default: {@code 60.0}
     */
    GAME_UPDATES_PER_SECOND(
      "game.updates.per.second",
      Double.class,
      60.0D
    ),

    /**
     * Maximum number of updates allowed per frame.
     * (Spiral of death protection).
     *
     * Default: {@code 5}
     */
    GAME_MAXIMUM_UPDATES_PER_FRAME(
      "game.maximum.updates.per.frame",
      Integer.class,
      5
    ),

    /**
     * Maximum accumulated time in seconds before resetting.
     * (Spiral of death protection).
     *
     * Default: {@code 0.5f} (500ms)
     */
    GAME_MAXIMUM_ACCUMULATED_TIME(
      "game.maximum.accumulated.time",
      Float.class,
      0.5F
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
     * @param _classType {@code Class<?>}
     * @param _defaultValue {@code Object}
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
