package es.noa.rad.game.engine.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

  /**
   *
   */
  @SuppressWarnings("unchecked")
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
    private final Map<String, Object> cache;

    /**
     *
     */
    private Configuration() {
      this.properties = new Properties();
      this.cache = new HashMap<>();
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
            "Unable to find resource file 'application.properties'");
        }
        this.properties.load(inputStream);
        this.validateRequiredProperties();
      } catch (
          final IOException iOException) {
        throw new RuntimeException(
          "Error reading resource file: 'application.properties'", iOException);
      } finally {
        if (inputStream != null) {
          try {
            inputStream.close();
          } catch (
              final IOException iOException) {
            iOException.printStackTrace();
          }
        }
      }
    }

    /**
     *
     * @throws IllegalStateException
     */
    private void validateRequiredProperties() {
      final String[] requiredProperties = {
        "window.width",
        "window.height",
        "window.title",
        "game.frequency.time"
      };

      for (final String propertyKey : requiredProperties) {
        if (this.properties.getProperty(propertyKey) == null) {
          throw new IllegalStateException(
            "Required property '" + propertyKey + "' is missing");
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
      final String cacheKey = _property + ":" + _classType.getName();

      /* Check cache first. */
      if (this.cache.containsKey(cacheKey)) {
        return (T) this.cache.get(cacheKey);
      }

      final String property = this.property(_property);
      if (property == null) {
        throw new IllegalArgumentException(
          "Property '" + _property + "' not found");
      }

      final T value = this.convertProperty(property, _classType);
      this.cache.put(cacheKey, value);
      return value;
    }

    /**
     *
     * @param <T> {@code <T>}
     * @param _property {@code String}
     * @param _defaultValue {@code T}
     * @return {@code <T>}
     */
    public <T> T property(
        final String _property,
        final T _defaultValue) {
      final Class<T> classType = (Class<T>) _defaultValue.getClass();
      final String cacheKey = _property + ":" + classType.getName();

      /* Check cache first. */
      if (this.cache.containsKey(cacheKey)) {
        return (T) this.cache.get(cacheKey);
      }

      final String property = this.property(_property);
      if (property == null) {
        return _defaultValue;
      }

      final T value = this.convertProperty(property, classType);
      this.cache.put(cacheKey, value);
      return value;
    }

    /**
     *
     * @param <T> {@code <T>}
     * @param _property {@code String}
     * @param _classType {@code Class<T>}
     * @return {@code <T>}
     */
    private <T> T convertProperty(
        final String _property,
        final Class<T> _classType) {
      if ((_classType == Integer.class)
       || (_classType == int.class)) {
        return (T) Integer.valueOf(_property);
      } else if ((_classType == Long.class)
       || (_classType == long.class)) {
        return (T) Long.valueOf(_property);
      } else if ((_classType == Boolean.class)
       || (_classType == boolean.class)) {
        return (T) Boolean.valueOf(_property);
      } else if ((_classType == Double.class)
       || (_classType == double.class)) {
        return (T) Double.valueOf(_property);
      } else {
        return (T) _property;
      }
    }

 }
