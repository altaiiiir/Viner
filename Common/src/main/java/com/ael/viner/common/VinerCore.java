package com.ael.viner.common;

/**
 * Core class for the Viner mod that provides loader-agnostic access to mod functionality. This
 * follows the singleton pattern to simplify access from different parts of the code.
 */
public class VinerCore {
  private static IVinerMod instance;

  // This should match the mod_id in gradle.properties
  public static final String MOD_ID = "viner";

  private VinerCore() {
    // Private constructor to prevent instantiation
  }

  /**
   * Set the mod instance. This should be called by the loader-specific entrypoint.
   *
   * @param mod The mod instance
   */
  public static void setInstance(IVinerMod mod) {
    instance = mod;
  }

  /**
   * Get the mod instance.
   *
   * @return The mod instance
   */
  public static IVinerMod get() {
    return instance;
  }

  /**
   * Get the player registry.
   *
   * @return The player registry
   */
  public static IPlayerRegistry getPlayerRegistry() {
    return instance.getPlayerRegistry();
  }

  /**
   * Get the config manager.
   *
   * @return The config manager
   */
  public static com.ael.viner.common.config.IConfigManager getConfigManager() {
    return instance.getConfigManager();
  }
}
