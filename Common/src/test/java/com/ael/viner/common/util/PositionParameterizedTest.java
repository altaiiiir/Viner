package com.ael.viner.common.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Parametrized tests for Position record to test various coordinate scenarios.
 */
class PositionParameterizedTest {

  @ParameterizedTest
  @DisplayName("Should create positions with various coordinate combinations")
  @CsvSource({
    "0,0,0",
    "1,2,3",
    "-1,-2,-3",
    "1000000,2000000,3000000",
    "-1000000,-2000000,-3000000",
    "0,-64,0",      // Minecraft world bottom
    "0,320,0",      // Minecraft world height limit
    "2147483647,-2147483648,0" // Integer min/max values
  })
  void createPositionsWithVariousCoordinates(int x, int y, int z) {
    // Act
    Position position = new Position(x, y, z);
    
    // Assert
    assertEquals(x, position.x());
    assertEquals(y, position.y());
    assertEquals(z, position.z());
  }

  @ParameterizedTest
  @DisplayName("Should correctly determine equality for various position pairs")
  @MethodSource("providePositionEqualityTestCases")
  void testPositionEquality(Position pos1, Position pos2, boolean expectedEqual, String description) {
    // Act & Assert
    if (expectedEqual) {
      assertEquals(pos1, pos2, description);
      assertEquals(pos1.hashCode(), pos2.hashCode(), "Hash codes should be equal for equal positions");
    } else {
      assertNotEquals(pos1, pos2, description);
    }
  }

  @ParameterizedTest
  @DisplayName("Should calculate Manhattan distance correctly")
  @CsvSource({
    "0,0,0, 0,0,0, 0",      // Same position
    "0,0,0, 1,0,0, 1",      // One unit apart on X
    "0,0,0, 0,1,0, 1",      // One unit apart on Y  
    "0,0,0, 0,0,1, 1",      // One unit apart on Z
    "0,0,0, 1,1,1, 3",      // Diagonal distance
    "0,0,0, -1,-1,-1, 3",   // Negative diagonal
    "10,20,30, 13,24,27, 10", // Complex case: |3| + |4| + |-3| = 10
    "-5,-10,-15, 5,10,15, 60" // Large distance: 10 + 20 + 30 = 60
  })
  void calculateManhattanDistance(int x1, int y1, int z1, int x2, int y2, int z2, int expectedDistance) {
    // Arrange
    Position pos1 = new Position(x1, y1, z1);
    Position pos2 = new Position(x2, y2, z2);
    
    // Act
    int actualDistance = manhattanDistance(pos1, pos2);
    
    // Assert
    assertEquals(expectedDistance, actualDistance);
  }

  @ParameterizedTest
  @DisplayName("Should determine adjacency correctly (Manhattan distance = 1)")
  @CsvSource({
    "0,0,0, 1,0,0, true",   // Adjacent on X
    "0,0,0, 0,1,0, true",   // Adjacent on Y
    "0,0,0, 0,0,1, true",   // Adjacent on Z
    "0,0,0, -1,0,0, true",  // Adjacent on negative X
    "0,0,0, 0,-1,0, true",  // Adjacent on negative Y
    "0,0,0, 0,0,-1, true",  // Adjacent on negative Z
    "0,0,0, 1,1,0, false",  // Diagonal (distance = 2)
    "0,0,0, 1,1,1, false",  // 3D diagonal (distance = 3)
    "0,0,0, 2,0,0, false",  // Two units apart
    "0,0,0, 0,0,0, false"   // Same position (distance = 0)
  })
  void determineAdjacency(int x1, int y1, int z1, int x2, int y2, int z2, boolean expectedAdjacent) {
    // Arrange
    Position pos1 = new Position(x1, y1, z1);
    Position pos2 = new Position(x2, y2, z2);
    
    // Act
    boolean actualAdjacent = areAdjacent(pos1, pos2);
    
    // Assert
    assertEquals(expectedAdjacent, actualAdjacent);
  }

  @ParameterizedTest
  @DisplayName("Should handle coordinate arithmetic operations")
  @CsvSource({
    "5,10,15, 2,3,4, 7,13,19",     // Addition
    "10,20,30, 3,7,12, 13,27,42",   // Addition (10+3=13, 20+7=27, 30+12=42)  
    "-5,-10,-15, 2,3,4, -3,-7,-11", // Addition with negatives
    "0,0,0, -1,-1,-1, -1,-1,-1"    // Addition resulting in negatives
  })
  void handleCoordinateArithmetic(int x1, int y1, int z1, int dx, int dy, int dz, 
                                  int expectedX, int expectedY, int expectedZ) {
    // Arrange
    Position original = new Position(x1, y1, z1);
    Position offset = new Position(dx, dy, dz);
    
    // Act
    Position result = addPositions(original, offset);
    
    // Assert
    assertEquals(expectedX, result.x());
    assertEquals(expectedY, result.y());
    assertEquals(expectedZ, result.z());
  }

  @ParameterizedTest
  @DisplayName("Should generate appropriate string representations")
  @CsvSource({
    "0,0,0",
    "1,2,3", 
    "-1,-2,-3",
    "1000000,2000000,3000000"
  })
  void generateStringRepresentation(int x, int y, int z) {
    // Arrange
    Position position = new Position(x, y, z);
    
    // Act
    String stringRepr = position.toString();
    
    // Assert
    assertNotNull(stringRepr);
    assertTrue(stringRepr.contains(String.valueOf(x)), "Should contain x coordinate");
    assertTrue(stringRepr.contains(String.valueOf(y)), "Should contain y coordinate");  
    assertTrue(stringRepr.contains(String.valueOf(z)), "Should contain z coordinate");
    assertTrue(stringRepr.length() > 5, "Should be a meaningful string representation");
  }

  /**
   * Provides test cases for position equality testing.
   */
  private static Stream<Arguments> providePositionEqualityTestCases() {
    return Stream.of(
        Arguments.of(new Position(0, 0, 0), new Position(0, 0, 0), true, "Same coordinates should be equal"),
        Arguments.of(new Position(1, 2, 3), new Position(1, 2, 3), true, "Identical positive coordinates"),
        Arguments.of(new Position(-1, -2, -3), new Position(-1, -2, -3), true, "Identical negative coordinates"),
        Arguments.of(new Position(0, 0, 0), new Position(1, 0, 0), false, "Different X coordinates"),
        Arguments.of(new Position(0, 0, 0), new Position(0, 1, 0), false, "Different Y coordinates"),
        Arguments.of(new Position(0, 0, 0), new Position(0, 0, 1), false, "Different Z coordinates"),
        Arguments.of(new Position(1, 2, 3), new Position(3, 2, 1), false, "Swapped coordinates"),
        Arguments.of(new Position(Integer.MAX_VALUE, 0, 0), new Position(Integer.MAX_VALUE, 0, 0), true, "Max integer values"),
        Arguments.of(new Position(Integer.MIN_VALUE, 0, 0), new Position(Integer.MIN_VALUE, 0, 0), true, "Min integer values")
    );
  }

  // Utility methods to test position operations
  private int manhattanDistance(Position pos1, Position pos2) {
    return Math.abs(pos1.x() - pos2.x()) + Math.abs(pos1.y() - pos2.y()) + Math.abs(pos1.z() - pos2.z());
  }

  private boolean areAdjacent(Position pos1, Position pos2) {
    return manhattanDistance(pos1, pos2) == 1;
  }

  private Position addPositions(Position pos1, Position pos2) {
    return new Position(pos1.x() + pos2.x(), pos1.y() + pos2.y(), pos1.z() + pos2.z());
  }
}
