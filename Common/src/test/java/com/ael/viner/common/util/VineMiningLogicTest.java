package com.ael.viner.common.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for VineMiningLogic class.
 * Tests the core vine mining algorithm without Minecraft dependencies.
 */
class VineMiningLogicTest {

  @Test
  @DisplayName("Should collect single block when no adjacent blocks match")
  void collectSingleBlock() {
    // Arrange
    Position start = new Position(0, 0, 0);
    String targetBlockId = "minecraft:stone";
    Function<Position, String> blockIdAt = pos -> 
        pos.equals(start) ? targetBlockId : "minecraft:air";
    
    // Act
    List<Position> result = VineMiningLogic.collectConnectedBlocks(
        start, targetBlockId, blockIdAt, 100);
    
    // Assert
    assertEquals(1, result.size());
    assertEquals(start, result.get(0));
  }

  @Test
  @DisplayName("Should collect connected blocks in a line")
  void collectConnectedLine() {
    // Arrange
    Position start = new Position(0, 0, 0);
    String targetBlockId = "minecraft:iron_ore";
    Function<Position, String> blockIdAt = pos -> {
      // Create a line of iron ore from (0,0,0) to (2,0,0)
      if (pos.y() == 0 && pos.z() == 0 && pos.x() >= 0 && pos.x() <= 2) {
        return targetBlockId;
      }
      return "minecraft:stone";
    };
    
    // Act
    List<Position> result = VineMiningLogic.collectConnectedBlocks(
        start, targetBlockId, blockIdAt, 100);
    
    // Assert
    assertEquals(3, result.size());
    assertTrue(result.contains(new Position(0, 0, 0)));
    assertTrue(result.contains(new Position(1, 0, 0)));
    assertTrue(result.contains(new Position(2, 0, 0)));
  }

  @Test
  @DisplayName("Should respect vineableLimit")
  void respectVineableLimit() {
    // Arrange
    Position start = new Position(0, 0, 0);
    String targetBlockId = "minecraft:diamond_ore";
    Function<Position, String> blockIdAt = pos -> {
      // Create a 3x3x3 cube of diamond ore
      if (Math.abs(pos.x()) <= 1 && Math.abs(pos.y()) <= 1 && Math.abs(pos.z()) <= 1) {
        return targetBlockId;
      }
      return "minecraft:stone";
    };
    int limit = 5;
    
    // Act
    List<Position> result = VineMiningLogic.collectConnectedBlocks(
        start, targetBlockId, blockIdAt, limit);
    
    // Assert
    assertEquals(limit, result.size());
    assertTrue(result.contains(start));
  }

  @Test
  @DisplayName("Should not collect non-matching blocks")
  void ignoreNonMatchingBlocks() {
    // Arrange
    Position start = new Position(0, 0, 0);
    String targetBlockId = "minecraft:coal_ore";
    Function<Position, String> blockIdAt = pos -> {
      if (pos.equals(start)) return targetBlockId;
      if (pos.equals(new Position(1, 0, 0))) return "minecraft:iron_ore"; // Different block type
      if (pos.equals(new Position(3, 0, 0))) return targetBlockId; // Same block but not connected (gap)
      return "minecraft:stone";
    };
    
    // Act
    List<Position> result = VineMiningLogic.collectConnectedBlocks(
        start, targetBlockId, blockIdAt, 100);
    
    // Assert
    assertEquals(1, result.size());
    assertEquals(start, result.get(0));
  }

  @Test
  @DisplayName("Should handle empty start position gracefully")
  void handleInvalidStartPosition() {
    // Arrange
    Position start = new Position(0, 0, 0);
    String targetBlockId = "minecraft:gold_ore";
    Function<Position, String> blockIdAt = pos -> "minecraft:stone"; // No matching blocks
    
    // Act
    List<Position> result = VineMiningLogic.collectConnectedBlocks(
        start, targetBlockId, blockIdAt, 100);
    
    // Assert
    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("Should collect blocks in 3D space")
  void collect3DConnectedBlocks() {
    // Arrange
    Position start = new Position(0, 0, 0);
    String targetBlockId = "minecraft:redstone_ore";
    Function<Position, String> blockIdAt = pos -> {
      // Create an L-shaped pattern in 3D
      if ((pos.x() == 0 && pos.y() == 0 && pos.z() >= 0 && pos.z() <= 2) ||
          (pos.x() >= 0 && pos.x() <= 2 && pos.y() == 0 && pos.z() == 0)) {
        return targetBlockId;
      }
      return "minecraft:stone";
    };
    
    // Act
    List<Position> result = VineMiningLogic.collectConnectedBlocks(
        start, targetBlockId, blockIdAt, 100);
    
    // Assert
    assertEquals(5, result.size()); // Center + 2 in Z direction + 2 in X direction
    assertTrue(result.contains(new Position(0, 0, 0)));
    assertTrue(result.contains(new Position(0, 0, 1)));
    assertTrue(result.contains(new Position(0, 0, 2)));
    assertTrue(result.contains(new Position(1, 0, 0)));
    assertTrue(result.contains(new Position(2, 0, 0)));
  }
}
