package com.ael.viner.common.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Enhanced parametrized tests for VineMiningLogic with comprehensive edge cases. Uses
 * parametrization to reduce duplication and test multiple scenarios efficiently.
 */
class VineMiningLogicParameterizedTest {

  @ParameterizedTest
  @DisplayName("Should respect different vineable limits")
  @ValueSource(ints = {1, 5, 10, 50, 100, 1000})
  void respectDifferentVineableLimits(int limit) {
    // Arrange - Create a large connected area (10x10x10 = 1000 blocks)
    Position start = new Position(0, 0, 0);
    String targetBlockId = "minecraft:diamond_ore";
    Function<Position, String> blockIdAt =
        pos -> {
          if (pos.x() >= 0
              && pos.x() < 10
              && pos.y() >= 0
              && pos.y() < 10
              && pos.z() >= 0
              && pos.z() < 10) {
            return targetBlockId;
          }
          return "minecraft:stone";
        };

    // Act
    List<Position> result =
        VineMiningLogic.collectConnectedBlocks(start, targetBlockId, blockIdAt, limit);

    // Assert
    assertEquals(Math.min(limit, 1000), result.size());
    assertTrue(result.contains(start));
  }

  @ParameterizedTest
  @DisplayName("Should handle different block patterns efficiently")
  @MethodSource("provideBlockPatterns")
  void handleDifferentBlockPatterns(
      String displayName,
      Function<Position, String> blockIdAt,
      Position start,
      String targetBlockId,
      int expectedSize) {
    // Act
    List<Position> result =
        VineMiningLogic.collectConnectedBlocks(start, targetBlockId, blockIdAt, 1000);

    // Assert
    assertEquals(
        expectedSize,
        result.size(),
        "Pattern '" + displayName + "' should produce " + expectedSize + " blocks");
    assertTrue(result.contains(start), "Result should contain starting position");
  }

  @ParameterizedTest
  @DisplayName("Should handle various coordinate ranges")
  @CsvSource({
    "0,0,0,1", // Origin
    "-5,-10,-15,1", // Negative coordinates
    "1000,2000,3000,1", // Large positive coordinates
    "0,255,-64,1", // Minecraft world height extremes
    "-30000000,0,30000000,1" // World border extremes
  })
  void handleVariousCoordinateRanges(int x, int y, int z, int expectedSize) {
    // Arrange
    Position start = new Position(x, y, z);
    String targetBlockId = "minecraft:test_ore";
    Function<Position, String> blockIdAt =
        pos -> pos.equals(start) ? targetBlockId : "minecraft:air";

    // Act
    List<Position> result =
        VineMiningLogic.collectConnectedBlocks(start, targetBlockId, blockIdAt, 100);

    // Assert
    assertEquals(expectedSize, result.size());
    assertEquals(start, result.get(0));
  }

  @ParameterizedTest
  @DisplayName("Should handle different block types and namespaces")
  @ValueSource(
      strings = {
        "minecraft:stone",
        "minecraft:iron_ore",
        "forge:custom_ore",
        "mymod:special_block",
        "namespace_with_underscores:block_with_underscores",
        "a:b" // Minimal valid resource location
      })
  void handleDifferentBlockTypes(String blockType) {
    // Arrange
    Position start = new Position(0, 0, 0);
    Function<Position, String> mockBlockIdAt = mock(Function.class);
    when(mockBlockIdAt.apply(start)).thenReturn(blockType);
    when(mockBlockIdAt.apply(any(Position.class))).thenReturn("minecraft:air");
    when(mockBlockIdAt.apply(start)).thenReturn(blockType); // Override for start position

    // Act
    List<Position> result =
        VineMiningLogic.collectConnectedBlocks(start, blockType, mockBlockIdAt, 10);

    // Assert
    assertEquals(1, result.size());
    assertEquals(start, result.get(0));

    // Verify the mock was called
    verify(mockBlockIdAt, atLeastOnce()).apply(start);
  }

  @Test
  @DisplayName("Should handle mock function with complex behavior")
  void handleMockFunctionWithComplexBehavior() {
    // Arrange
    Position start = new Position(0, 0, 0);
    String targetBlockId = "minecraft:coal_ore";
    Function<Position, String> mockBlockIdAt = mock(Function.class);

    // Setup complex mock behavior - be more specific with the default
    when(mockBlockIdAt.apply(new Position(0, 0, 0))).thenReturn(targetBlockId);
    when(mockBlockIdAt.apply(new Position(1, 0, 0))).thenReturn(targetBlockId);
    when(mockBlockIdAt.apply(new Position(0, 1, 0))).thenReturn(targetBlockId);
    when(mockBlockIdAt.apply(new Position(-1, 0, 0))).thenReturn("minecraft:stone");
    when(mockBlockIdAt.apply(new Position(0, -1, 0))).thenReturn("minecraft:stone");
    when(mockBlockIdAt.apply(new Position(0, 0, 1))).thenReturn("minecraft:stone");
    when(mockBlockIdAt.apply(new Position(0, 0, -1))).thenReturn("minecraft:stone");
    // Don't use catch-all any() as it can interfere with specific mocks

    // Act
    List<Position> result =
        VineMiningLogic.collectConnectedBlocks(start, targetBlockId, mockBlockIdAt, 10);

    // Assert
    assertEquals(3, result.size());
    assertTrue(result.contains(new Position(0, 0, 0)));
    assertTrue(result.contains(new Position(1, 0, 0)));
    assertTrue(result.contains(new Position(0, 1, 0)));

    // Verify mock interactions
    verify(mockBlockIdAt, atLeast(3)).apply(any(Position.class));
  }

  @Test
  @DisplayName("Should handle performance with large connected areas")
  void handlePerformanceWithLargeAreas() {
    // Arrange - Create a 20x20x20 connected area
    Position start = new Position(0, 0, 0);
    String targetBlockId = "minecraft:massive_ore";
    Function<Position, String> blockIdAt =
        pos -> {
          if (pos.x() >= 0
              && pos.x() < 20
              && pos.y() >= 0
              && pos.y() < 20
              && pos.z() >= 0
              && pos.z() < 20) {
            return targetBlockId;
          }
          return "minecraft:stone";
        };

    long startTime = System.nanoTime();

    // Act
    List<Position> result =
        VineMiningLogic.collectConnectedBlocks(start, targetBlockId, blockIdAt, 8000);

    long endTime = System.nanoTime();
    long duration = (endTime - startTime) / 1_000_000; // Convert to milliseconds

    // Assert
    assertEquals(8000, result.size()); // All 8000 blocks in the 20x20x20 area
    assertTrue(duration < 1000, "Algorithm should complete within 1 second for 8000 blocks");
    assertTrue(result.contains(start));
  }

  @Test
  @DisplayName("Should handle circular/complex patterns without infinite loops")
  void handleCircularPatterns() {
    // Arrange - Create a hollow sphere pattern
    Position start = new Position(0, 0, 0);
    String targetBlockId = "minecraft:sphere_ore";
    Function<Position, String> blockIdAt =
        pos -> {
          double distance = Math.sqrt(pos.x() * pos.x() + pos.y() * pos.y() + pos.z() * pos.z());
          // Hollow sphere: blocks at distance 4-6 from center
          if (distance >= 4.0 && distance <= 6.0) {
            return targetBlockId;
          }
          return "minecraft:air";
        };

    // Act
    List<Position> result =
        VineMiningLogic.collectConnectedBlocks(
            new Position(0, 0, 5), targetBlockId, blockIdAt, 1000);

    // Assert
    assertTrue(result.size() > 50, "Should find many blocks in hollow sphere");
    assertTrue(result.size() < 1000, "Should not hit the limit");

    // Verify it's a connected component
    for (Position pos : result) {
      double distance = Math.sqrt(pos.x() * pos.x() + pos.y() * pos.y() + pos.z() * pos.z());
      assertTrue(
          distance >= 4.0 && distance <= 6.0,
          "All results should be within the hollow sphere bounds");
    }
  }

  /** Provides different block patterns for parametrized testing. */
  private static Stream<Arguments> provideBlockPatterns() {
    return Stream.of(
        // Single block
        Arguments.of(
            "Single Block",
            (Function<Position, String>)
                pos -> pos.equals(new Position(0, 0, 0)) ? "minecraft:single" : "minecraft:air",
            new Position(0, 0, 0),
            "minecraft:single",
            1),

        // Straight line (X-axis)
        Arguments.of(
            "Straight Line X",
            (Function<Position, String>)
                pos ->
                    (pos.y() == 0 && pos.z() == 0 && pos.x() >= 0 && pos.x() <= 4)
                        ? "minecraft:line"
                        : "minecraft:air",
            new Position(0, 0, 0),
            "minecraft:line",
            5),

        // L-shape
        Arguments.of(
            "L-Shape",
            (Function<Position, String>)
                pos -> {
                  if (pos.y() == 0 && pos.z() == 0) {
                    return (pos.x() >= 0 && pos.x() <= 2)
                            || (pos.x() == 0 && pos.z() >= 0 && pos.z() <= 2)
                        ? "minecraft:lshape"
                        : "minecraft:air";
                  }
                  if (pos.x() == 0 && pos.y() == 0 && pos.z() >= 0 && pos.z() <= 2) {
                    return "minecraft:lshape";
                  }
                  return "minecraft:air";
                },
            new Position(0, 0, 0),
            "minecraft:lshape",
            5 // 3 blocks in X + 2 additional in Z (center counted once)
            ),

        // Small cube
        Arguments.of(
            "Small Cube 2x2x2",
            (Function<Position, String>)
                pos ->
                    (pos.x() >= 0
                            && pos.x() <= 1
                            && pos.y() >= 0
                            && pos.y() <= 1
                            && pos.z() >= 0
                            && pos.z() <= 1)
                        ? "minecraft:cube"
                        : "minecraft:air",
            new Position(0, 0, 0),
            "minecraft:cube",
            8));
  }
}
