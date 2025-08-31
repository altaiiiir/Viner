package com.ael.viner.common.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit tests for Position record. Tests the record functionality and basic operations. */
class PositionTest {

  @Test
  @DisplayName("Should create position with correct coordinates")
  void createPosition() {
    // Arrange & Act
    Position position = new Position(10, 20, 30);

    // Assert
    assertEquals(10, position.x());
    assertEquals(20, position.y());
    assertEquals(30, position.z());
  }

  @Test
  @DisplayName("Should handle negative coordinates")
  void handleNegativeCoordinates() {
    // Arrange & Act
    Position position = new Position(-5, -10, -15);

    // Assert
    assertEquals(-5, position.x());
    assertEquals(-10, position.y());
    assertEquals(-15, position.z());
  }

  @Test
  @DisplayName("Should support equality comparison")
  void testEquality() {
    // Arrange
    Position position1 = new Position(1, 2, 3);
    Position position2 = new Position(1, 2, 3);
    Position position3 = new Position(1, 2, 4);

    // Assert
    assertEquals(position1, position2);
    assertNotEquals(position1, position3);
  }

  @Test
  @DisplayName("Should support hashCode for collections")
  void testHashCode() {
    // Arrange
    Position position1 = new Position(5, 10, 15);
    Position position2 = new Position(5, 10, 15);

    // Assert
    assertEquals(position1.hashCode(), position2.hashCode());
  }

  @Test
  @DisplayName("Should generate meaningful toString")
  void testToString() {
    // Arrange
    Position position = new Position(100, 200, 300);

    // Act
    String result = position.toString();

    // Assert
    assertTrue(result.contains("100"));
    assertTrue(result.contains("200"));
    assertTrue(result.contains("300"));
  }
}
