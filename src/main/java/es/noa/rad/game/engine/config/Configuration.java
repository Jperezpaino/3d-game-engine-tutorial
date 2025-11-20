package es.noa.rad.game.engine.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration class to load application properties.
 * Implements Singleton pattern for global access to configuration.
 */
public final class Configuration {

  /** Singleton instance. */
  private static Configuration instance;

  /** Properties object. */
  private final Properties properties;

  /**
   * Private constructor to prevent instantiation.
   * Loads properties from application.properties file.
   */
  private Configuration() {
    this.properties = new Properties();
    try (InputStream input = Configuration.class
        .getClassLoader()
        .getResourceAsStream("application.properties")) {
      if (input == null) {
        throw new IOException("Unable to find application.properties");
      }
      this.properties.load(input);
    } catch (final IOException ioException) {
      ioException.printStackTrace();
      throw new RuntimeException("Failed to load configuration", ioException);
    }
  }

  /**
   * Gets the singleton instance of Configuration.
   *
   * @return the Configuration instance
   */
  public static synchronized Configuration get() {
    if (Configuration.instance == null) {
      Configuration.instance = new Configuration();
    }
    return Configuration.instance;
  }

  /**
   * Gets a string property value.
   *
   * @param _key the property key
   * @return the property value
   */
  public String getProperty(final String _key) {
    return this.properties.getProperty(_key);
  }

  /**
   * Gets an integer property value.
   *
   * @param _key the property key
   * @return the property value as integer
   */
  public int getIntProperty(final String _key) {
    return Integer.parseInt(this.properties.getProperty(_key));
  }

  /**
   * Gets a long property value.
   *
   * @param _key the property key
   * @return the property value as long
   */
  public long getLongProperty(final String _key) {
    return Long.parseLong(this.properties.getProperty(_key));
  }

  /**
   * Gets the window width from configuration.
   *
   * @return the window width
   */
  public int getWindowWidth() {
    return this.getIntProperty("window.width");
  }

  /**
   * Gets the window height from configuration.
   *
   * @return the window height
   */
  public int getWindowHeight() {
    return this.getIntProperty("window.height");
  }

  /**
   * Gets the window title from configuration.
   *
   * @return the window title
   */
  public String getWindowTitle() {
    return this.getProperty("window.title");
  }

  /**
   * Gets the frame time from configuration.
   *
   * @return the frame time in milliseconds
   */
  public long getFrameTime() {
    return this.getLongProperty("game.frame.time");
  }

}
