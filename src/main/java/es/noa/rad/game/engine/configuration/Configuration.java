package es.noa.rad.game.engine.configuration;

import java.io.IOException;
import java.io.InputStream;
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
    private Configuration() {
      this.properties = new Properties();
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
      } catch (
          final IOException iOException) {
        throw new RuntimeException(
          "Error reading resource file: 'application.properties'", iOException);
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
      final String property = this.property(_property);
      if (property == null) {
        throw new IllegalArgumentException(
          "Property '" + _property + "' not found");
      }
      if ((_classType == Integer.class)
       || (_classType == int.class)) {
        return (T) Integer.valueOf(property);
      } else if ((_classType == Long.class)
       || (_classType == long.class)) {
        return (T) Long.valueOf(property);
      } else if ((_classType == Boolean.class)
       || (_classType == boolean.class)) {
        return (T) Boolean.valueOf(property);
      } else if ((_classType == Double.class)
       || (_classType == double.class)) {
        return (T) Double.valueOf(property);
      } else {
        return (T) property;
      }
    }

 }
