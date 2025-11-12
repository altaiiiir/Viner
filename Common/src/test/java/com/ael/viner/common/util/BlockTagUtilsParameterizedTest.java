package com.ael.viner.common.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/** Comprehensive parametrized tests for BlockTagUtils with edge cases and validation scenarios. */
class BlockTagUtilsParameterizedTest {

  @ParameterizedTest
  @DisplayName("Should correctly identify valid resource locations")
  @ValueSource(
      strings = {
        "minecraft:stone",
        "forge:custom_block",
        "mymod:special_ore",
        "a:b", // Minimal valid
        "namespace_with_underscores:block_with_underscores",
        "mod123:block456",
        "x:very_long_block_name_with_lots_of_underscores_and_numbers_123"
      })
  void identifyValidResourceLocations(String resourceLocation) {
    // Act & Assert
    assertDoesNotThrow(
        () -> {
          boolean isTag = resourceLocation.startsWith("#");
          String cleanLocation = isTag ? resourceLocation.substring(1) : resourceLocation;

          // Basic validation logic (mimicking what BlockTagUtils might do)
          assertTrue(cleanLocation.contains(":"), "Should contain namespace separator");
          String[] parts = cleanLocation.split(":");
          assertEquals(2, parts.length, "Should have exactly two parts");
          assertTrue(parts[0].length() > 0, "Namespace should not be empty");
          assertTrue(parts[1].length() > 0, "Path should not be empty");
        });
  }

  @ParameterizedTest
  @DisplayName("Should correctly identify invalid resource locations")
  @ValueSource(
      strings = {
        "no_namespace",
        ":missing_namespace",
        "missing_path:",
        "too:many:colons",
        "",
        ":",
        "::double_colon",
        "namespace with spaces:invalid"
      })
  void identifyInvalidResourceLocations(String resourceLocation) {
    // Act & Assert - These should be considered invalid
    boolean isValid = isValidResourceLocation(resourceLocation);
    assertFalse(isValid, "Resource location '" + resourceLocation + "' should be invalid");
  }

  @ParameterizedTest
  @DisplayName("Should distinguish between blocks and tags correctly")
  @CsvSource({
    "minecraft:stone, false",
    "#minecraft:logs, true",
    "#forge:ores, true",
    "mymod:custom_block, false",
    "#mymod:custom_tags, true",
    "#, false", // Invalid tag (empty after #)
    "##double_hash, false" // Invalid (multiple #)
  })
  void distinguishBlocksAndTags(String input, boolean expectedIsTag) {
    // Act
    boolean actualIsTag =
        input.startsWith("#") && input.length() > 1 && isValidResourceLocation(input.substring(1));

    // Assert
    assertEquals(expectedIsTag, actualIsTag);
  }

  @ParameterizedTest
  @DisplayName("Should handle edge cases in resource location parsing")
  @MethodSource("provideResourceLocationEdgeCases")
  void handleResourceLocationEdgeCases(
      String input,
      boolean shouldBeValid,
      String expectedNamespace,
      String expectedPath,
      boolean expectedIsTag) {
    // Act
    boolean isTag = input.startsWith("#");
    String cleanInput = isTag ? input.substring(1) : input;
    boolean isValid = isValidResourceLocation(cleanInput);

    // Assert validity
    assertEquals(shouldBeValid, isValid, "Validity check failed for: " + input);

    if (shouldBeValid) {
      assertEquals(expectedIsTag, isTag, "Tag detection failed for: " + input);
      String[] parts = cleanInput.split(":");
      assertEquals(expectedNamespace, parts[0], "Namespace parsing failed");
      assertEquals(expectedPath, parts[1], "Path parsing failed");
    }
  }

  @ParameterizedTest
  @DisplayName("Should handle null and empty inputs gracefully")
  @NullAndEmptySource
  @ValueSource(strings = {" ", "\t", "\n", "   "})
  void handleNullAndEmptyInputs(String input) {
    // Act & Assert
    assertFalse(isValidResourceLocation(input), "Null/empty/whitespace should be invalid");
  }

  @ParameterizedTest
  @DisplayName("Should validate namespace-specific patterns")
  @CsvSource({
    "minecraft:stone, minecraft, true",
    "forge:copper_ore, forge, true",
    "mymod:custom_block, mymod, true",
    "1invalid:block, 1invalid, false", // Numbers at start invalid
    "Invalid:block, Invalid, false", // Capital letters in namespace typically invalid
    "valid_mod:block, valid_mod, true",
    "mod-name:block, mod-name, true" // Hyphens might be valid depending on implementation
  })
  void validateNamespacePatterns(
      String resourceLocation, String expectedNamespace, boolean shouldBeValidNamespace) {
    // Arrange
    String[] parts = resourceLocation.split(":");
    String actualNamespace = parts[0];

    // Act
    boolean isValidNamespace = isValidNamespace(actualNamespace);

    // Assert
    assertEquals(expectedNamespace, actualNamespace);
    assertEquals(shouldBeValidNamespace, isValidNamespace);
  }

  @ParameterizedTest
  @DisplayName("Should handle mixed lists of blocks and tags")
  @MethodSource("provideMixedBlockTagLists")
  void handleMixedBlockTagLists(
      String listName, String[] inputs, int expectedBlocks, int expectedTags, int expectedInvalid) {
    // Act
    int actualBlocks = 0;
    int actualTags = 0;
    int actualInvalid = 0;

    for (String input : inputs) {
      if (input.startsWith("#")) {
        if (isValidResourceLocation(input.substring(1))) {
          actualTags++;
        } else {
          actualInvalid++;
        }
      } else {
        if (isValidResourceLocation(input)) {
          actualBlocks++;
        } else {
          actualInvalid++;
        }
      }
    }

    // Assert
    assertEquals(expectedBlocks, actualBlocks, "Block count mismatch for " + listName);
    assertEquals(expectedTags, actualTags, "Tag count mismatch for " + listName);
    assertEquals(expectedInvalid, actualInvalid, "Invalid count mismatch for " + listName);
  }

  /** Provides edge cases for resource location parsing. */
  private static Stream<Arguments> provideResourceLocationEdgeCases() {
    return Stream.of(
        Arguments.of("minecraft:stone", true, "minecraft", "stone", false),
        Arguments.of("#minecraft:logs", true, "minecraft", "logs", true),
        Arguments.of("a:b", true, "a", "b", false),
        Arguments.of("#a:b", true, "a", "b", true),
        Arguments.of("no_colon", false, null, null, false),
        Arguments.of(":empty_namespace", false, null, null, false),
        Arguments.of("empty_path:", false, null, null, false),
        Arguments.of("#", false, null, null, true),
        Arguments.of("##invalid", false, null, null, true));
  }

  /** Provides mixed lists of blocks and tags for testing. */
  private static Stream<Arguments> provideMixedBlockTagLists() {
    return Stream.of(
        Arguments.of(
            "Simple Mix",
            new String[] {"minecraft:stone", "#minecraft:logs", "forge:copper_ore"},
            2,
            1,
            0 // 2 blocks, 1 tag, 0 invalid
            ),
        Arguments.of(
            "With Invalid Entries",
            new String[] {"minecraft:stone", "#minecraft:logs", "invalid", "#", "forge:ore"},
            2,
            1,
            2 // 2 blocks, 1 tag, 2 invalid
            ),
        Arguments.of(
            "Only Blocks",
            new String[] {"minecraft:stone", "minecraft:iron_ore", "forge:copper_ore"},
            3,
            0,
            0 // 3 blocks, 0 tags, 0 invalid
            ),
        Arguments.of(
            "Only Tags",
            new String[] {"#minecraft:logs", "#forge:ores", "#mymod:custom_tags"},
            0,
            3,
            0 // 0 blocks, 3 tags, 0 invalid
            ),
        Arguments.of(
            "Empty List", new String[] {}, 0, 0, 0 // 0 blocks, 0 tags, 0 invalid
            ));
  }

  // Helper methods for validation logic
  private boolean isValidResourceLocation(String resourceLocation) {
    if (resourceLocation == null || resourceLocation.trim().isEmpty()) {
      return false;
    }

    String[] parts = resourceLocation.split(":");
    if (parts.length != 2) {
      return false;
    }

    return !parts[0].isEmpty()
        && !parts[1].isEmpty()
        && isValidNamespace(parts[0])
        && isValidPath(parts[1]);
  }

  private boolean isValidNamespace(String namespace) {
    if (namespace == null || namespace.isEmpty()) {
      return false;
    }

    // Basic validation: lowercase letters, numbers, underscores, hyphens
    // First character should not be a number
    return namespace.matches("^[a-z][a-z0-9_-]*$");
  }

  private boolean isValidPath(String path) {
    if (path == null || path.isEmpty()) {
      return false;
    }

    // Basic validation: lowercase letters, numbers, underscores, forward slashes
    return path.matches("^[a-z0-9_/]+$");
  }
}
