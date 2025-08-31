package com.ael.viner.forge;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ael.viner.common.IPlayerRegistry;
import com.ael.viner.common.VinerCore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for VinerForge class.
 * Tests Forge-specific mod initialization and singleton behavior.
 * 
 * Note: These tests focus on unit testing without full Minecraft environment.
 * Integration tests would require a different approach with Minecraft Test Framework.
 */
class VinerForgeTest {

  @AfterEach
  void tearDown() {
    // Clean up singleton state
    VinerCore.setInstance(null);
  }

  @Test
  @DisplayName("Should have correct MOD_ID constant")
  void correctModId() {
    // Assert
    assertEquals("viner", VinerForge.MOD_ID);
    assertEquals(VinerCore.MOD_ID, VinerForge.MOD_ID);
  }

  @Test
  @DisplayName("Should implement IVinerMod interface")
  void implementsIVinerMod() {
    // This test would require mocking Forge environment
    // For now, we just verify the class structure
    assertTrue(com.ael.viner.common.IVinerMod.class.isAssignableFrom(VinerForge.class));
  }

  @Test
  @DisplayName("Should return null getInstance when VinerCore has different instance type")
  void getInstanceReturnsNullForWrongType() {
    // Arrange
    com.ael.viner.common.IVinerMod mockMod = mock(com.ael.viner.common.IVinerMod.class);
    VinerCore.setInstance(mockMod);
    
    // Act
    VinerForge result = VinerForge.getInstance();
    
    // Assert
    assertNull(result);
  }

  // Note: Full constructor testing would require mocking Forge's ModLoadingContext,
  // FMLJavaModLoadingContext, and MinecraftForge.EVENT_BUS.
  // This would be better suited for integration tests or with a proper Forge test harness.
}
