package es.noa.rad.game.engine.configuration.settings;

import es.noa.rad.game.engine.configuration.Configuration;

  /**
   *
   */
  public enum GameSettings {

    /**
     *
     */
    GAME_FRAMES_PER_SECOND(
      "game.frames.per.second",
      Double.class
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
