package es.noa.rad.game.engine.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

  /**
   *
   */
  public final class Configuration {

    /**
     *
     */
    private static final String CONFIG_PATH =
      "es/noa/rad/game/settings/application.properties";

    /**
     *
     */
    private static Configuration instance = null;

    /**
     *
     */
    private final Properties properties;

    /**
     *
     */
    private final Map<String, Object> propertiesCache;

    /**
     *
     */
    private Configuration() {
      this.properties = new Properties();
      this.propertiesCache = new HashMap<>();
    }

    /**
     *
     */
    private static synchronized void createInstance() {
      /*
       * Synchronized creator to protect against possible multi-threading
       * problems.
       */
      if (Configuration.instance == null) {
        Configuration.instance = new Configuration();
      }
    }

    /**
     *
     * @return {@code Configuration}
     */
    public static Configuration get() {
      if (Configuration.instance == null) {
        Configuration.createInstance();
      }
      return Configuration.instance;
    }

    /**
     *
     * @throws RuntimeException
     */
    public void init() {
      InputStream inputStream = null;
      try {
        inputStream = Configuration.class
          .getClassLoader().getResourceAsStream(Configuration.CONFIG_PATH);
        if (inputStream == null) {
          throw new IOException(
            "Unable to find resource file 'application.properties'"
          );
        }
        this.properties.load(inputStream);
      } catch (
          final IOException iOException) {
        throw new RuntimeException(
          "Error reading resource file: 'application.properties'",
          iOException
        );
      } finally {
        try {
          if (inputStream != null) {
            inputStream.close();
          }
        } catch (
            final IOException iOException) {
          throw new RuntimeException(
            "Error closing resource file: 'application.properties'",
            iOException
          );
        }
      }
    }

    /**
     *
     * @param _property {@code String}
     * @return {@code String}
     */
    public String property(
        final String _property) {
      return this.properties.getProperty(_property);
    }

    /**
     *
     * @param <T> {@code <T>}
     * @param _property {@code String}
     * @param _classType {@code Class<T>}
     * @return {@code <T>}
     * @throws IllegalArgumentException
     */
    public <T> T property(
        final String _property,
        final Class<T> _classType) {

      /* Check cache first. */
      final String cacheKey = _property + ":" + _classType.getName();
      if (this.propertiesCache.containsKey(cacheKey)) {
        return (T) this.propertiesCache.get(cacheKey);
      }

      final String property = this.property(_property);
      if (property == null) {
        throw new IllegalArgumentException(
          "Property '" + _property + "' not found");
      }

      T value = null;
      if ((_classType == Integer.class)
       || (_classType == int.class)) {
        value = (T) Integer.valueOf(property);
      } else if ((_classType == Long.class)
       || (_classType == long.class)) {
        value = (T) Long.valueOf(property);
      } else if ((_classType == Boolean.class)
       || (_classType == boolean.class)) {
        value = (T) Boolean.valueOf(property);
      } else if ((_classType == Double.class)
       || (_classType == double.class)) {
        value = (T) Double.valueOf(property);
      } else {
        value = (T) property;
      }

      this.propertiesCache.put(cacheKey, value);
      return value;
    }

    /**
     *
     * @param <T> {@code <T>}
     * @param _property {@code String}
     * @param _classType {@code Class<T>}
     * @param _defaultValue {@code <T>}
     * @return {@code <T>}
     */
    public <T> T property(
        final String _property,
        final Class<T> _classType,
        final T _defaultValue) {
      try {
        return this.property(_property, _classType);
      } catch (
          final IllegalArgumentException illegalArgumentException) {
        /* Cache the default value. */
        final String cacheKey = _property + ":" + _classType.getName();
        this.propertiesCache.put(cacheKey, _defaultValue);
        return _defaultValue;
      }
    }

  }
