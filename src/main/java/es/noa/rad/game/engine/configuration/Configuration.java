package es.noa.rad.game.engine.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

  /**
   * Singleton configuration manager for loading and accessing properties.
   *
   * <p>This class provides centralized access to application configuration
   * from the {@code application.properties} file. Features:
   * <ul>
   *   <li>Type-safe property retrieval with automatic conversion</li>
   *   <li>Thread-safe caching for performance</li>
   *   <li>Default value support</li>
   *   <li>Support for all primitive types and String</li>
   * </ul>
   *
   * <p>Thread-safe singleton with lazy initialization and concurrent cache.
   *
   * <p>Usage example:
   * <pre>{@code
   * Configuration.get().init();
   * int width = Configuration.get().property(
   *     "window.width",
   *     Integer.class
   * );
   * String title = Configuration.get().property(
   *     "window.title",
   *     String.class,
   *     "Default Title"
   * );
   * }</pre>
   *
   * @see WindowSettings
   * @see GameSettings
   */
  public final class Configuration {

    /**
     * Path to the application properties file in classpath.
     */
    private static final String CONFIG_PATH =
      "es/noa/rad/game/settings/application.properties";

    /**
     * Singleton instance of the configuration manager.
     */
    private static Configuration instance = null;

    /**
     * Flag indicating if configuration has been loaded.
     * Must be true before accessing properties.
     */
    private boolean initialized;

    /**
     * Properties loaded from the configuration file.
     */
    private final Properties properties;

    /**
     * Thread-safe cache for parsed property values.
     * Key format: "property.name:ClassName"
     */
    private final Map<String, Object> propertiesCache;

    /**
     * Private constructor to enforce singleton pattern.
     * Initializes properties and cache structures.
     */
    private Configuration() {
      this.initialized = false;
      this.properties = new Properties();
      this.propertiesCache = new ConcurrentHashMap<>();
    }

    /**
     * Creates the singleton instance in a thread-safe manner.
     * Uses synchronized method to prevent multiple instances
     * in multi-threaded environments.
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
     * Gets the singleton instance of the configuration manager.
     * Creates the instance on first call (lazy initialization).
     *
     * @return the singleton {@code Configuration} instance
     */
    public static Configuration get() {
      if (Configuration.instance == null) {
        Configuration.createInstance();
      }
      return Configuration.instance;
    }

    /**
     * Loads the application properties file from classpath.
     *
     * <p>Reads properties from {@code application.properties} located at:
     * {@code es/noa/rad/game/settings/application.properties}
     *
     * <p>This method must be called before accessing any properties.
     *
     * @throws RuntimeException if the properties file cannot be found,
     *     read, or closed properly
     */
    public void init() {
      InputStream inputStream = null;
      try {
        /* Load properties file from classpath. */
        inputStream = Configuration.class
          .getClassLoader().getResourceAsStream(Configuration.CONFIG_PATH);
        if (inputStream == null) {
          throw new IOException(
            "Unable to find resource file 'application.properties'"
          );
        }
        this.properties.load(inputStream);
        this.initialized = true;
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
     * Verifies that configuration has been initialized.
     *
     * @throws IllegalStateException if {@link #init()} has not been called
     */
    private void initialized() {
      if (!this.initialized) {
        throw new IllegalStateException(
          "Configuration must be initialized. Call Configuration.get().init()."
        );
      }
    }

    /**
     * Gets a raw property value as a String.
     *
     * @param _property the property key
     * @return the property value as String, or null if not found
     */
    private String property(
        final String _property) {
      return this.properties.getProperty(_property);
    }

    /**
     * Gets a typed property value with automatic conversion and caching.
     *
     * <p>Supports conversion to the following types:
     * <ul>
     *   <li>Byte, byte</li>
     *   <li>Short, short</li>
     *   <li>Integer, int</li>
     *   <li>Long, long</li>
     *   <li>Float, float</li>
     *   <li>Double, double</li>
     *   <li>Boolean, boolean</li>
     *   <li>Character, char (first character of string)</li>
     *   <li>String</li>
     * </ul>
     *
     * <p>Values are cached after first retrieval for performance.
     *
     * @param <T> the type to convert the property to
     * @param _property the property key
     * @param _classType the class of the desired return type
     * @return the property value converted to type T
     * @throws IllegalStateException if configuration not initialized
     * @throws IllegalArgumentException if property not found
     */
    @SuppressWarnings("unchecked")
    public <T> T property(
        final String _property,
        final Class<T> _classType) {

      /* Verified that the configuration is initialized. */
      this.initialized();

      /* Check cache first for performance. */
      final String cacheKey = _property + ":" + _classType.getName();
      if (this.propertiesCache.containsKey(cacheKey)) {
        return (T) this.propertiesCache.get(cacheKey);
      }

      final String property = this.property(_property);
      if (property == null) {
        throw new IllegalArgumentException(
          "Property '" + _property + "' not found");
      }

      /* Convert string property to requested type. */
      T value = null;
      if ((_classType == Byte.class)
       || (_classType == byte.class)) {
        value = (T) Byte.valueOf(property);
      } else if ((_classType == Short.class)
       || (_classType == short.class)) {
        value = (T) Short.valueOf(property);
      } else if ((_classType == Integer.class)
       || (_classType == int.class)) {
        value = (T) Integer.valueOf(property);
      } else if ((_classType == Long.class)
       || (_classType == long.class)) {
        value = (T) Long.valueOf(property);
      } else if ((_classType == Float.class)
       || (_classType == float.class)) {
        value = (T) Float.valueOf(property);
      } else if ((_classType == Double.class)
       || (_classType == double.class)) {
        value = (T) Double.valueOf(property);
      } else if ((_classType == Boolean.class)
       || (_classType == boolean.class)) {
        value = (T) Boolean.valueOf(property);
      } else if ((_classType == Character.class)
       || (_classType == char.class)) {
        /* For Character, we take the first character of the string. */
       if ((property != null)
        && (!property.isEmpty())) {
         value = (T) Character.valueOf(property.charAt(0));
        } else {
         value = (T) Character.valueOf('\0');
        }
      } else {
        value = (T) property;
      }

      /* Cache the converted value for future requests. */
      this.propertiesCache.put(cacheKey, value);
      return value;
    }

    /**
     * Gets a typed property value with a default fallback.
     *
     * <p>If the property is not found, returns the default value
     * and caches it for future requests.
     *
     * <p>This is useful for optional configuration values.
     *
     * @param <T> the type to convert the property to
     * @param _property the property key
     * @param _classType the class of the desired return type
     * @param _defaultValue the value to return if property not found
     * @return the property value, or default value if not found
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
