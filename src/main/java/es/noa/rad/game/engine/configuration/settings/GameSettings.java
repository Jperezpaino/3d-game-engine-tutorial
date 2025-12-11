package es.noa.rad.game.engine.configuration.settings;

import es.noa.rad.game.engine.configuration.Configuration;

  /**
   *
   */
  public enum GameSettings {

    /**
     *
     */
    GAME_VERTICAL_SYNCHRONIZATION(
      "game.vertical.synchronization",
      Boolean.class
    ),

    /**
     *
     */
    GAME_UPDATES_PER_SECOND(
      "game.updates.per.second",
      Double.class
    ),

    /**
     *
     */
    GAME_MAXIMUM_UPDATES_PER_FRAME(
      "game.maximum.updates.per.frame",
      Integer.class
    ),

    /**
     *
     */
    GAME_MAXIMUM_ACCUMULATED_TIME(
      "game.maximum.accumulated.time",
      Float.class
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
     * @param _classType {@code Class<?>}
     */
    GameSettings(
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
