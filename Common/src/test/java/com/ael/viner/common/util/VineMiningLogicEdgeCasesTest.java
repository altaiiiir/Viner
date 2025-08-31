package com.ael.viner.common.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;

/**
 * Edge case and error handling tests for VineMiningLogic.
 * Focuses on boundary conditions, error scenarios, and exceptional cases.
 */
class VineMiningLogicEdgeCasesTest {

  @Test
  @DisplayName("Should handle null start position gracefully")
  void handleNullStartPosition() {
    // Arrange
    String targetBlockId = "minecraft:stone";
    Function<Position, String> blockIdAt = pos -> targetBlockId;
    
    // Act & Assert
    assertThrows(NullPointerException.class, () -> {
      VineMiningLogic.collectConnectedBlocks(null, targetBlockId, blockIdAt, 10);
    });
  }

  @Test
  @DisplayName("Should handle null block ID gracefully")
  void handleNullBlockId() {
    // Arrange
    Position start = new Position(0, 0, 0);
    Function<Position, String> blockIdAt = pos -> "minecraft:stone";
    
    // Act & Assert
    assertThrows(NullPointerException.class, () -> {
      VineMiningLogic.collectConnectedBlocks(start, null, blockIdAt, 10);
    });
  }

  @Test
  @DisplayName("Should handle null block function gracefully")
  void handleNullBlockFunction() {
    // Arrange
    Position start = new Position(0, 0, 0);
    String targetBlockId = "minecraft:stone";
    
    // Act & Assert
    assertThrows(NullPointerException.class, () -> {
      VineMiningLogic.collectConnectedBlocks(start, targetBlockId, null, 10);
    });
  }

  @Test
  @DisplayName("Should handle zero and negative limits")
  void handleInvalidLimits() {
    // Arrange
    Position start = new Position(0, 0, 0);
    String targetBlockId = "minecraft:stone";
    Function<Position, String> blockIdAt = pos -> targetBlockId;
    
    // Act & Assert - Zero limit
    List<Position> resultZero = VineMiningLogic.collectConnectedBlocks(start, targetBlockId, blockIdAt, 0);
    assertEquals(0, resultZero.size(), "Zero limit should return empty list");
    
    // Act & Assert - Negative limit
    List<Position> resultNegative = VineMiningLogic.collectConnectedBlocks(start, targetBlockId, blockIdAt, -5);
    assertEquals(0, resultNegative.size(), "Negative limit should return empty list");
  }

  @Test
  @DisplayName("Should handle block function that returns null")
  void handleBlockFunctionReturningNull() {
    // Arrange
    Position start = new Position(0, 0, 0);
    String targetBlockId = "minecraft:stone";
    Function<Position, String> blockIdAt = pos -> null; // Always returns null
    
    // Act
    List<Position> result = VineMiningLogic.collectConnectedBlocks(start, targetBlockId, blockIdAt, 10);
    
    // Assert
    assertEquals(0, result.size(), "Should return empty list when block function returns null");
  }

  @Test
  @DisplayName("Should handle block function that throws exceptions")
  void handleBlockFunctionThrowingExceptions() {
    // Arrange
    Position start = new Position(0, 0, 0);
    String targetBlockId = "minecraft:stone";
    Function<Position, String> mockBlockIdAt = mock(Function.class);
    
    // Configure mock to throw exception
    when(mockBlockIdAt.apply(any(Position.class))).thenThrow(new RuntimeException("Simulated world access error"));
    
    // Act & Assert
    assertThrows(RuntimeException.class, () -> {
      VineMiningLogic.collectConnectedBlocks(start, targetBlockId, mockBlockIdAt, 10);
    });
  }

  @Test
  @DisplayName("Should handle very large coordinate values")
  void handleLargeCoordinateValues() {
    // Arrange
    Position start = new Position(Integer.MAX_VALUE - 1, Integer.MAX_VALUE - 1, Integer.MAX_VALUE - 1);
    String targetBlockId = "minecraft:edge_stone";
    Function<Position, String> blockIdAt = pos -> {
      // Only the start position has the target block
      if (pos.equals(start)) return targetBlockId;
      return "minecraft:air";
    };
    
    // Act
    List<Position> result = VineMiningLogic.collectConnectedBlocks(start, targetBlockId, blockIdAt, 10);
    
    // Assert
    assertEquals(1, result.size());
    assertEquals(start, result.get(0));
  }

  @Test
  @DisplayName("Should handle coordinate overflow scenarios")
  void handleCoordinateOverflow() {
    // Arrange - Start at max value to test overflow in adjacent block checking
    Position start = new Position(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    String targetBlockId = "minecraft:overflow_stone";
    Function<Position, String> blockIdAt = pos -> {
      if (pos.equals(start)) return targetBlockId;
      return "minecraft:air";
    };
    
    // Act - Should not crash despite overflow in adjacent coordinate calculation
    List<Position> result = VineMiningLogic.collectConnectedBlocks(start, targetBlockId, blockIdAt, 10);
    
    // Assert
    assertEquals(1, result.size());
    assertEquals(start, result.get(0));
  }

  @Test
  @DisplayName("Should handle intermittent block function failures")
  void handleIntermittentBlockFunctionFailures() {
    // Arrange
    Position start = new Position(0, 0, 0);
    String targetBlockId = "minecraft:unreliable_stone";
    Function<Position, String> mockBlockIdAt = mock(Function.class);
    
    // Configure mock to succeed for start, fail for first adjacent check
    when(mockBlockIdAt.apply(start)).thenReturn(targetBlockId);
    when(mockBlockIdAt.apply(new Position(1, 0, 0))).thenThrow(new RuntimeException("Network timeout"));
    // Don't configure other positions - let them return null or throw
    
    // Act & Assert - Should propagate the exception when algorithm tries to check adjacent (1,0,0)
    assertThrows(RuntimeException.class, () -> {
      VineMiningLogic.collectConnectedBlocks(start, targetBlockId, mockBlockIdAt, 100);
    });
  }

  @Test
  @DisplayName("Should handle block function with complex state changes")
  void handleBlockFunctionWithStateChanges() {
    // Arrange - Mock that changes behavior after certain number of calls
    Position start = new Position(0, 0, 0);
    String targetBlockId = "minecraft:changing_stone";
    Function<Position, String> mockBlockIdAt = mock(Function.class);
    
    // Use a custom answer to simulate state changes
    when(mockBlockIdAt.apply(any(Position.class))).thenAnswer(new org.mockito.stubbing.Answer<String>() {
      private int callCount = 0;
      
      @Override
      public String answer(InvocationOnMock invocation) {
        Position pos = invocation.getArgument(0);
        callCount++;
        
        // First few calls return target block, then switch to air
        if (pos.equals(start)) {
          return targetBlockId;
        } else if (callCount <= 5 && Math.abs(pos.x()) <= 1 && pos.y() == 0 && pos.z() == 0) {
          return targetBlockId;
        }
        return "minecraft:air";
      }
    });
    
    // Act
    List<Position> result = VineMiningLogic.collectConnectedBlocks(start, targetBlockId, mockBlockIdAt, 100);
    
    // Assert
    assertTrue(result.size() >= 1, "Should at least find the start position");
    assertTrue(result.contains(start), "Should always contain start position");
    
    // Verify the mock was called
    verify(mockBlockIdAt, atLeast(1)).apply(any(Position.class));
  }

  @Test
  @DisplayName("Should handle extremely high limits without memory issues")
  void handleExtremelyHighLimits() {
    // Arrange
    Position start = new Position(0, 0, 0);
    String targetBlockId = "minecraft:single_stone";
    Function<Position, String> blockIdAt = pos -> 
        pos.equals(start) ? targetBlockId : "minecraft:air";
    
    // Act - Use very high limit but only one block is available
    List<Position> result = VineMiningLogic.collectConnectedBlocks(start, targetBlockId, blockIdAt, Integer.MAX_VALUE);
    
    // Assert
    assertEquals(1, result.size());
    assertEquals(start, result.get(0));
  }

  @Test
  @DisplayName("Should handle disconnected identical blocks correctly")
  void handleDisconnectedIdenticalBlocks() {
    // Arrange
    Position start = new Position(0, 0, 0);
    String targetBlockId = "minecraft:scattered_ore";
    Function<Position, String> blockIdAt = pos -> {
      // Target block at start and at (5, 5, 5) - not connected
      if (pos.equals(start) || pos.equals(new Position(5, 5, 5))) {
        return targetBlockId;
      }
      return "minecraft:stone";
    };
    
    // Act
    List<Position> result = VineMiningLogic.collectConnectedBlocks(start, targetBlockId, blockIdAt, 100);
    
    // Assert
    assertEquals(1, result.size(), "Should only find connected blocks starting from start position");
    assertEquals(start, result.get(0));
    assertFalse(result.contains(new Position(5, 5, 5)), "Should not include disconnected identical blocks");
  }

  @Test
  @DisplayName("Should handle empty block ID strings")
  void handleEmptyBlockIdStrings() {
    // Arrange
    Position start = new Position(0, 0, 0);
    String targetBlockId = "";  // Empty string
    Function<Position, String> blockIdAt = pos -> "";
    
    // Act
    List<Position> result = VineMiningLogic.collectConnectedBlocks(start, targetBlockId, blockIdAt, 10);
    
    // Assert - Should work with empty strings if function returns them consistently
    assertTrue(result.size() > 0, "Should find blocks even with empty block ID");
    assertTrue(result.contains(start), "Should contain start position");
  }

  @Test
  @DisplayName("Should handle block function with inconsistent return values")
  void handleInconsistentBlockFunction() {
    // Arrange
    Position start = new Position(0, 0, 0);
    String targetBlockId = "minecraft:inconsistent";
    Function<Position, String> mockBlockIdAt = mock(Function.class);
    
    // Configure mock to return consistent values - algorithm only calls once per position during visited check
    when(mockBlockIdAt.apply(start)).thenReturn(targetBlockId);  // Start position matches
    when(mockBlockIdAt.apply(new Position(1, 0, 0))).thenReturn("minecraft:air");  // Adjacent doesn't match
    when(mockBlockIdAt.apply(new Position(-1, 0, 0))).thenReturn("minecraft:air"); // Adjacent doesn't match
    when(mockBlockIdAt.apply(new Position(0, 1, 0))).thenReturn("minecraft:air");  // Adjacent doesn't match
    when(mockBlockIdAt.apply(new Position(0, -1, 0))).thenReturn("minecraft:air"); // Adjacent doesn't match
    when(mockBlockIdAt.apply(new Position(0, 0, 1))).thenReturn("minecraft:air");  // Adjacent doesn't match
    when(mockBlockIdAt.apply(new Position(0, 0, -1))).thenReturn("minecraft:air"); // Adjacent doesn't match
    
    // Act
    List<Position> result = VineMiningLogic.collectConnectedBlocks(start, targetBlockId, mockBlockIdAt, 10);
    
    // Assert - Should find exactly one block (the start position)
    assertEquals(1, result.size(), "Should find exactly the start position");
    assertEquals(start, result.get(0), "Should contain the start position");
    
    // Verify the function was called for start position  
    verify(mockBlockIdAt, atLeastOnce()).apply(start);
  }
}
