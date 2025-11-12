package com.ael.viner.forge.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit tests for Config class. Tests configuration default values, validation, and structure. */
class ConfigTest {

  @Test
  @DisplayName("Should have valid default values for all configuration options")
  void hasValidDefaults() {
    // Test that all config specs have been properly defined
    assertNotNull(Config.SPEC);
    assertNotNull(Config.BUILDER);

    // Test numeric configuration defaults are within reasonable ranges
    assertTrue(Config.VINEABLE_LIMIT.getDefault() > 0, "Vineable limit should be positive");
    assertTrue(Config.VINEABLE_LIMIT.getDefault() <= 10000, "Vineable limit should be reasonable");

    assertTrue(
        Config.EXHAUSTION_PER_BLOCK.getDefault() >= 0.0, "Exhaustion should be non-negative");
    assertTrue(Config.EXHAUSTION_PER_BLOCK.getDefault() <= 10.0, "Exhaustion should be reasonable");

    // Test dimension configuration defaults
    assertTrue(Config.HEIGHT_ABOVE.getDefault() >= 0, "Height above should be non-negative");
    assertTrue(Config.HEIGHT_BELOW.getDefault() >= 0, "Height below should be non-negative");
    assertTrue(Config.WIDTH_LEFT.getDefault() >= 0, "Width left should be non-negative");
    assertTrue(Config.WIDTH_RIGHT.getDefault() >= 0, "Width right should be non-negative");
    assertTrue(Config.LAYER_OFFSET.getDefault() >= 0, "Layer offset should be non-negative");
  }

  @Test
  @DisplayName("Should have non-null default collections")
  void hasNonNullCollectionDefaults() {
    assertNotNull(Config.VINEABLE_BLOCKS.getDefault());
    assertNotNull(Config.UNVINEABLE_BLOCKS.getDefault());
  }

  @Test
  @DisplayName("Should have boolean configurations with valid defaults")
  void hasBooleanDefaults() {
    assertNotNull(Config.SHAPE_VINE.getDefault());
    assertNotNull(Config.VINE_ALL.getDefault());
  }

  @Test
  @DisplayName("Should have reasonable default values")
  void hasReasonableDefaultValues() {
    // Test that default values are within sensible ranges

    // Vineable limit should be reasonable
    int vineableLimit = Config.VINEABLE_LIMIT.getDefault();
    assertTrue(vineableLimit >= 1, "Minimum vineable limit should be at least 1");
    assertTrue(vineableLimit <= 100000, "Maximum vineable limit should be reasonable");

    // Exhaustion should be reasonable
    double exhaustion = Config.EXHAUSTION_PER_BLOCK.getDefault();
    assertTrue(exhaustion >= 0.0, "Minimum exhaustion should be non-negative");
    assertTrue(exhaustion <= 100.0, "Maximum exhaustion should be reasonable");
  }

  @Test
  @DisplayName("Should maintain configuration consistency")
  void maintainsConfigurationConsistency() {
    // Test that related configurations have consistent defaults
    int heightAbove = Config.HEIGHT_ABOVE.getDefault();
    int heightBelow = Config.HEIGHT_BELOW.getDefault();
    int widthLeft = Config.WIDTH_LEFT.getDefault();
    int widthRight = Config.WIDTH_RIGHT.getDefault();

    // These should form a reasonable mining area
    int totalHeight = heightAbove + heightBelow + 1;
    int totalWidth = widthLeft + widthRight + 1;

    assertTrue(totalHeight > 0, "Total mining height should be positive");
    assertTrue(totalWidth > 0, "Total mining width should be positive");
    assertTrue(
        totalHeight * totalWidth <= Config.VINEABLE_LIMIT.getDefault(),
        "Mining area should not exceed vineable limit by default");
  }
}
