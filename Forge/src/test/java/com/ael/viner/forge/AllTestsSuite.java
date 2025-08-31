package com.ael.viner.forge;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Comprehensive test suite for all Forge module tests.
 * Runs all tests in the Forge module with detailed reporting.
 */
@Suite
@SuiteDisplayName("⚒️ Viner Forge Module - Complete Test Suite")
@SelectPackages("com.ael.viner.forge")
public class AllTestsSuite {
  // This class serves as a comprehensive test suite runner
  // All tests in the com.ael.viner.forge package and subpackages will be executed
  
  // Test Coverage:
  // ✅ VinerForge - Forge mod initialization and integration
  // ✅ Config - Configuration validation and defaults
  // ✅ VinerPlayerData - Player-specific configuration management
  // ✅ AbstractPacket - Network packet serialization/deserialization
  // ✅ ConfigEventHandler - Configuration event handling
  // ✅ MiningUtils - Mining utility functions and validation
}
