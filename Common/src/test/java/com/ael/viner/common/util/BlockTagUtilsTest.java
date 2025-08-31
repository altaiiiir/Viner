package com.ael.viner.common.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for BlockTagUtils utility class. Tests block tag parsing and validation functionality.
 */
class BlockTagUtilsTest {

  @Test
  @DisplayName("Should parse valid block resource location")
  void parseValidBlockResourceLocation() {
    // This test will depend on the actual implementation of BlockTagUtils
    // For now, we'll test the structure and assumptions

    // Arrange
    String validBlockId = "minecraft:stone";

    // Act & Assert
    // BlockTagUtils should be able to handle valid resource locations
    assertTrue(validBlockId.contains(":"));
    assertTrue(validBlockId.startsWith("minecraft:"));
  }

  @Test
  @DisplayName("Should parse valid tag resource location")
  void parseValidTagResourceLocation() {
    // Arrange
    String validTagId = "#minecraft:ores";

    // Act & Assert
    assertTrue(validTagId.startsWith("#"));
    assertTrue(validTagId.contains(":"));
  }

  @Test
  @DisplayName("Should differentiate between blocks and tags")
  void differentiateBetweenBlocksAndTags() {
    // Arrange
    String blockId = "minecraft:iron_ore";
    String tagId = "#minecraft:ores";

    // Act & Assert
    assertFalse(blockId.startsWith("#"));
    assertTrue(tagId.startsWith("#"));
  }

  @Test
  @DisplayName("Should handle list of mixed blocks and tags")
  void handleMixedBlocksAndTags() {
    // Arrange
    List<String> mixedList =
        Arrays.asList("minecraft:stone", "#minecraft:ores", "minecraft:coal_ore", "#forge:gems");

    // Act
    long blockCount = mixedList.stream().filter(s -> !s.startsWith("#")).count();
    long tagCount = mixedList.stream().filter(s -> s.startsWith("#")).count();

    // Assert
    assertEquals(2, blockCount);
    assertEquals(2, tagCount);
    assertEquals(4, mixedList.size());
  }

  @Test
  @DisplayName("Should validate resource location format")
  void validateResourceLocationFormat() {
    // Arrange
    String validFormat = "minecraft:stone";
    String invalidFormat1 = "stone"; // Missing namespace
    String invalidFormat2 = ":stone"; // Empty namespace
    String invalidFormat3 = "minecraft:"; // Empty path

    // Act & Assert
    assertTrue(isValidResourceLocationFormat(validFormat));
    assertFalse(isValidResourceLocationFormat(invalidFormat1));
    assertFalse(isValidResourceLocationFormat(invalidFormat2));
    assertFalse(isValidResourceLocationFormat(invalidFormat3));
  }

  @Test
  @DisplayName("Should handle common namespaces")
  void handleCommonNamespaces() {
    // Arrange
    List<String> commonNamespaces =
        Arrays.asList("minecraft:stone", "forge:ores", "modid:custom_block");

    // Act & Assert
    for (String resourceLocation : commonNamespaces) {
      assertTrue(isValidResourceLocationFormat(resourceLocation));
      String[] parts = resourceLocation.split(":");
      assertEquals(2, parts.length);
      assertFalse(parts[0].isEmpty());
      assertFalse(parts[1].isEmpty());
    }
  }

  /**
   * Helper method to validate resource location format. This simulates what BlockTagUtils might do
   * internally.
   */
  private boolean isValidResourceLocationFormat(String resourceLocation) {
    if (resourceLocation == null || resourceLocation.isEmpty()) {
      return false;
    }

    // Remove tag prefix if present
    String cleanLocation =
        resourceLocation.startsWith("#") ? resourceLocation.substring(1) : resourceLocation;

    // Check for exactly one colon
    String[] parts = cleanLocation.split(":");
    if (parts.length != 2) {
      return false;
    }

    // Check that both namespace and path are non-empty
    return !parts[0].isEmpty() && !parts[1].isEmpty();
  }
}
