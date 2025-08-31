package com.ael.viner.common;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for VinerCore class.
 * Tests the singleton pattern and mod instance management.
 */
class VinerCoreTest {

  private IVinerMod mockMod;
  private IPlayerRegistry mockPlayerRegistry;

  @BeforeEach
  void setUp() {
    mockMod = mock(IVinerMod.class);
    mockPlayerRegistry = mock(IPlayerRegistry.class);
    when(mockMod.getPlayerRegistry()).thenReturn(mockPlayerRegistry);
  }

  @AfterEach
  void tearDown() {
    // Reset singleton state for clean tests
    VinerCore.setInstance(null);
  }

  @Test
  @DisplayName("Should set and get mod instance")
  void setAndGetInstance() {
    // Act
    VinerCore.setInstance(mockMod);
    
    // Assert
    assertSame(mockMod, VinerCore.get());
  }

  @Test
  @DisplayName("Should return null when no instance is set")
  void returnNullWhenNoInstance() {
    // Assert
    assertNull(VinerCore.get());
  }

  @Test
  @DisplayName("Should get player registry from mod instance")
  void getPlayerRegistry() {
    // Arrange
    VinerCore.setInstance(mockMod);
    
    // Act
    IPlayerRegistry result = VinerCore.getPlayerRegistry();
    
    // Assert
    assertSame(mockPlayerRegistry, result);
    verify(mockMod).getPlayerRegistry();
  }

  @Test
  @DisplayName("Should throw exception when getting player registry with no instance")
  void throwExceptionWhenGettingPlayerRegistryWithNoInstance() {
    // Assert
    assertThrows(NullPointerException.class, () -> {
      VinerCore.getPlayerRegistry();
    });
  }

  @Test
  @DisplayName("Should have correct MOD_ID constant")
  void correctModId() {
    // Assert
    assertEquals("viner", VinerCore.MOD_ID);
  }

  @Test
  @DisplayName("Should allow instance replacement")
  void allowInstanceReplacement() {
    // Arrange
    IVinerMod firstMod = mock(IVinerMod.class);
    IVinerMod secondMod = mock(IVinerMod.class);
    
    // Act
    VinerCore.setInstance(firstMod);
    assertEquals(firstMod, VinerCore.get());
    
    VinerCore.setInstance(secondMod);
    
    // Assert
    assertSame(secondMod, VinerCore.get());
    assertNotSame(firstMod, VinerCore.get());
  }
}
