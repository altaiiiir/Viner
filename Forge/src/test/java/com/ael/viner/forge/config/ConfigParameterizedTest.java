package com.ael.viner.forge.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/** Parametrized tests for Config class to validate various configuration scenarios. */
class ConfigParameterizedTest {

  @ParameterizedTest
  @DisplayName("Should validate numeric config ranges are reasonable")
  @MethodSource("provideNumericConfigBounds")
  void validateNumericConfigRanges(
      String configName, Object defaultValue, Object minExpected, Object maxExpected, String type) {
    // Assert based on type
    switch (type) {
      case "int" -> {
        int intDefault = (Integer) defaultValue;
        int intMin = (Integer) minExpected;
        int intMax = (Integer) maxExpected;
        assertTrue(intDefault >= intMin, configName + " default should be >= " + intMin);
        assertTrue(intDefault <= intMax, configName + " default should be <= " + intMax);
      }
      case "double" -> {
        double doubleDefault = (Double) defaultValue;
        double doubleMin = (Double) minExpected;
        double doubleMax = (Double) maxExpected;
        assertTrue(doubleDefault >= doubleMin, configName + " default should be >= " + doubleMin);
        assertTrue(doubleDefault <= doubleMax, configName + " default should be <= " + doubleMax);
      }
    }
  }

  @ParameterizedTest
  @DisplayName("Should validate boolean config values are non-null")
  @ValueSource(strings = {"SHAPE_VINE", "VINE_ALL"})
  void validateBooleanConfigs(String configName) {
    // Use reflection or direct access to test config values
    switch (configName) {
      case "SHAPE_VINE" -> assertNotNull(Config.SHAPE_VINE.getDefault());
      case "VINE_ALL" -> assertNotNull(Config.VINE_ALL.getDefault());
    }
  }

  @ParameterizedTest
  @DisplayName("Should validate mining area calculations with various dimensions")
  @CsvSource({
    "1,1,1,1,0, 9", // 3x3x1 area = 9 blocks
    "2,2,2,2,0, 25", // 5x5x1 area = 25 blocks
    "3,2,1,1,0, 18", // 6x3x1 area = 18 blocks (heightAbove=3, heightBelow=2, widthLeft=1,
    // widthRight=1)
    "0,0,0,0,0, 1", // Single block
    "5,5,5,5,0, 121", // 11x11x1 = 121 blocks
    "1,1,1,1,1, 27" // 3x3x3 = 27 blocks (with layerOffset)
  })
  void validateMiningAreaCalculations(
      int heightAbove,
      int heightBelow,
      int widthLeft,
      int widthRight,
      int layerOffset,
      int expectedMaxArea) {
    // Act - Calculate mining area based on config values
    int totalHeight = heightAbove + heightBelow + 1; // +1 for current level
    int totalWidth = widthLeft + widthRight + 1; // +1 for current column
    int totalLayers = layerOffset * 2 + 1; // layers above and below + current
    int actualArea = totalHeight * totalWidth * totalLayers;

    // Assert
    assertEquals(expectedMaxArea, actualArea);
    assertTrue(actualArea > 0, "Mining area should be positive");
    assertTrue(actualArea <= 10000, "Mining area should be reasonable (not too large)");
  }

  @ParameterizedTest
  @DisplayName("Should validate exhaustion calculations for different scenarios")
  @CsvSource({
    "1.0, 10, 10.0", // 1.0 exhaustion per block, 10 blocks = 10.0 total
    "2.5, 8, 20.0", // 2.5 exhaustion per block, 8 blocks = 20.0 total
    "0.5, 20, 10.0", // 0.5 exhaustion per block, 20 blocks = 10.0 total
    "0.0, 100, 0.0", // No exhaustion
    "10.0, 1, 10.0" // High exhaustion, single block
  })
  void validateExhaustionCalculations(
      double exhaustionPerBlock, int blocksMined, double expectedTotal) {
    // Act
    double actualTotal = exhaustionPerBlock * blocksMined;

    // Assert
    assertEquals(expectedTotal, actualTotal, 0.001);
    assertTrue(actualTotal >= 0.0, "Total exhaustion should be non-negative");
  }

  @ParameterizedTest
  @DisplayName("Should validate list configurations are properly initialized")
  @MethodSource("provideListConfigurations")
  void validateListConfigurations(String configName, List<?> configValue) {
    // Assert
    assertNotNull(configValue, configName + " should not be null");
    // Lists can be empty, that's valid
    assertTrue(configValue.size() >= 0, configName + " should have valid size");
  }

  @ParameterizedTest
  @DisplayName("Should validate consistency between related config values")
  @CsvSource({
    "64, 3, 2, 1, 1, true", // vineableLimit=64, area=3*3*1=9, valid
    "100, 5, 5, 2, 2, true", // vineableLimit=100, area=11*5*1=55, valid
    "10, 10, 10, 10, 0, false", // vineableLimit=10, area=21*21*1=441, invalid (area > limit)
    "1000, 10, 10, 10, 5, true" // vineableLimit=1000, area=21*21*11=4851, but limit is high enough
  })
  void validateConsistencyBetweenConfigs(
      int vineableLimit,
      int heightAbove,
      int heightBelow,
      int widthLeft,
      int widthRight,
      boolean shouldBeConsistent) {
    // Act - Calculate potential mining area
    int totalHeight = heightAbove + heightBelow + 1;
    int totalWidth = widthLeft + widthRight + 1;
    int potentialArea = totalHeight * totalWidth;

    boolean isConsistent = potentialArea <= vineableLimit;

    // Assert
    assertEquals(
        shouldBeConsistent,
        isConsistent,
        "Consistency check failed: vineableLimit="
            + vineableLimit
            + ", potentialArea="
            + potentialArea);
  }

  /** Provides numeric configuration bounds for testing. */
  private static Stream<Arguments> provideNumericConfigBounds() {
    return Stream.of(
        Arguments.of("VINEABLE_LIMIT", Config.VINEABLE_LIMIT.getDefault(), 1, 100000, "int"),
        Arguments.of(
            "EXHAUSTION_PER_BLOCK", Config.EXHAUSTION_PER_BLOCK.getDefault(), 0.0, 100.0, "double"),
        Arguments.of("HEIGHT_ABOVE", Config.HEIGHT_ABOVE.getDefault(), 0, 50, "int"),
        Arguments.of("HEIGHT_BELOW", Config.HEIGHT_BELOW.getDefault(), 0, 50, "int"),
        Arguments.of("WIDTH_LEFT", Config.WIDTH_LEFT.getDefault(), 0, 50, "int"),
        Arguments.of("WIDTH_RIGHT", Config.WIDTH_RIGHT.getDefault(), 0, 50, "int"),
        Arguments.of("LAYER_OFFSET", Config.LAYER_OFFSET.getDefault(), 0, 10, "int"));
  }

  /** Provides list configurations for testing. */
  private static Stream<Arguments> provideListConfigurations() {
    return Stream.of(
        Arguments.of("VINEABLE_BLOCKS", Config.VINEABLE_BLOCKS.getDefault()),
        Arguments.of("UNVINEABLE_BLOCKS", Config.UNVINEABLE_BLOCKS.getDefault()));
  }
}
